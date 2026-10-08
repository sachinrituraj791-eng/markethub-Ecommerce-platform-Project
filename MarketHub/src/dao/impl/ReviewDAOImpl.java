package dao.impl;

import dao.ReviewDAO;
import exception.DatabaseException;
import model.Review;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ReviewDAOImpl
 * JDBC Implementation of ReviewDAO.
 */
public class ReviewDAOImpl implements ReviewDAO {

    @Override
    public List<Review> findByProductId(int productId) {
        String sql = "SELECT r.*, u.full_name AS user_name " +
                     "FROM reviews r JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.product_id = ? ORDER BY r.created_at DESC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setReviewId(rs.getInt("review_id"));
                    r.setProductId(rs.getInt("product_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setUserName(rs.getString("user_name"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving reviews: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean hasUserReviewedProduct(int userId, int productId) {
        String sql = "SELECT 1 FROM reviews WHERE user_id = ? AND product_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking existing review: " + e.getMessage(), e);
        }
    }

    @Override
    public double calculateAverageRating(int productId) {
        String sql = "SELECT COALESCE(AVG(rating), 0.0) FROM reviews WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating product rating: " + e.getMessage(), e);
        }
        return 0.0;
    }

    @Override
    public Review save(Review review) {
        String insertSql = "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        String updateProductSql = 
            "UPDATE products p SET " +
            "rating = (SELECT AVG(rating) FROM reviews WHERE product_id = ?), " +
            "review_count = (SELECT COUNT(*) FROM reviews WHERE product_id = ?) " +
            "WHERE p.product_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.beginTransaction();
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, review.getProductId());
                ps.setInt(2, review.getUserId());
                ps.setInt(3, review.getRating());
                ps.setString(4, review.getComment());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) review.setReviewId(keys.getInt(1));
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateProductSql)) {
                psUpdate.setInt(1, review.getProductId());
                psUpdate.setInt(2, review.getProductId());
                psUpdate.setInt(3, review.getProductId());
                psUpdate.executeUpdate();
            }

            DBConnection.commit(conn);
            return review;
        } catch (SQLException e) {
            DBConnection.rollback(conn);
            throw new DatabaseException("Error saving review: " + e.getMessage(), e);
        } finally {
            DBConnection.close(conn);
        }
    }

    @Override
    public Optional<Review> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public List<Review> findAll() {
        return new ArrayList<>();
    }

    @Override
    public boolean update(Review entity) {
        return false;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting review: " + e.getMessage(), e);
        }
    }
}
