package dao.impl;

import dao.WishlistDAO;
import exception.DatabaseException;
import model.Product;
import model.WishlistItem;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * WishlistDAOImpl
 * JDBC Implementation of WishlistDAO.
 */
public class WishlistDAOImpl implements WishlistDAO {

    @Override
    public List<WishlistItem> findByUserId(int userId) {
        String sql = "SELECT w.*, p.name AS product_name, p.image_url, p.price, p.discount_percent, p.stock_quantity " +
                     "FROM wishlist w " +
                     "JOIN products p ON w.product_id = p.product_id " +
                     "WHERE w.user_id = ? ORDER BY w.added_at DESC";
        List<WishlistItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WishlistItem item = new WishlistItem();
                    item.setWishlistId(rs.getInt("wishlist_id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setProductId(rs.getInt("product_id"));
                    Timestamp ts = rs.getTimestamp("added_at");
                    if (ts != null) item.setAddedAt(ts.toLocalDateTime());

                    Product p = new Product();
                    p.setProductId(item.getProductId());
                    p.setName(rs.getString("product_name"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setDiscountPercent(rs.getInt("discount_percent"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    item.setProduct(p);

                    list.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving wishlist: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean addItem(int userId, int productId) {
        String sql = "INSERT IGNORE INTO wishlist (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error adding to wishlist: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeItem(int userId, int productId) {
        String sql = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error removing from wishlist: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isInWishlist(int userId, int productId) {
        String sql = "SELECT 1 FROM wishlist WHERE user_id = ? AND product_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking wishlist item: " + e.getMessage(), e);
        }
    }
}
