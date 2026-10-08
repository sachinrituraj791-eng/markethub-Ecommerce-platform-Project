package dao.impl;

import dao.ProductDAO;
import exception.DatabaseException;
import model.Product;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ProductDAOImpl
 * Direct JDBC implementation for product catalog, search, filtering, and stock adjustments.
 */
public class ProductDAOImpl implements ProductDAO {

    @Override
    public Optional<Product> findById(Integer id) {
        String sql = "SELECT p.*, c.name AS category_name, u.full_name AS seller_name " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN users u ON p.seller_id = u.user_id " +
                     "WHERE p.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding product by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT p.*, c.name AS category_name, u.full_name AS seller_name " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN users u ON p.seller_id = u.user_id " +
                     "WHERE p.is_active = TRUE AND p.is_approved = TRUE " +
                     "ORDER BY p.product_id DESC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching products: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Product> findFiltered(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name AS category_name, u.full_name AS seller_name " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN users u ON p.seller_id = u.user_id " +
                "WHERE p.is_active = TRUE AND p.is_approved = TRUE "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
            sql.append("AND p.price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            sql.append("AND p.price <= ? ");
            params.add(maxPrice);
        }

        // Sorting mapping
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.price ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.price DESC ");
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.rating DESC ");
        } else {
            sql.append("ORDER BY p.created_at DESC ");
        }

        int offset = Math.max(0, (page - 1) * limit);
        sql.append("LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error in filtered product query: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int countFiltered(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM products p " +
                "WHERE p.is_active = TRUE AND p.is_approved = TRUE "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
            sql.append("AND p.price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            sql.append("AND p.price <= ? ");
            params.add(maxPrice);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting filtered products: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<Product> findBySellerId(int sellerId) {
        String sql = "SELECT p.*, c.name AS category_name, u.full_name AS seller_name " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN users u ON p.seller_id = u.user_id " +
                     "WHERE p.seller_id = ? " +
                     "ORDER BY p.product_id DESC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying seller products: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Product> findFeatured(int limit) {
        String sql = "SELECT p.*, c.name AS category_name, u.full_name AS seller_name " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN users u ON p.seller_id = u.user_id " +
                     "WHERE p.is_active = TRUE AND p.is_approved = TRUE " +
                     "ORDER BY p.rating DESC, p.review_count DESC LIMIT ?";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying featured products: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Product> findLowStock(int sellerId, int threshold) {
        String sql = "SELECT p.*, c.name AS category_name, u.full_name AS seller_name " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN users u ON p.seller_id = u.user_id " +
                     "WHERE p.seller_id = ? AND p.stock_quantity <= ? " +
                     "ORDER BY p.stock_quantity ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            ps.setInt(2, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking low stock items: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Product save(Product product) {
        String sql = "INSERT INTO products (seller_id, category_id, name, slug, description, price, discount_percent, stock_quantity, image_url, is_approved, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, product.getSellerId());
            ps.setInt(2, product.getCategoryId());
            ps.setString(3, product.getName());
            ps.setString(4, product.getSlug());
            ps.setString(5, product.getDescription());
            ps.setBigDecimal(6, product.getPrice());
            ps.setInt(7, product.getDiscountPercent());
            ps.setInt(8, product.getStockQuantity());
            ps.setString(9, product.getImageUrl());
            ps.setBoolean(10, product.isApproved());
            ps.setBoolean(11, product.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        product.setProductId(keys.getInt(1));
                    }
                }
            }
            return product;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating product: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, discount_percent = ?, stock_quantity = ?, image_url = ?, category_id = ?, is_active = ? " +
                     "WHERE product_id = ? AND seller_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getDiscountPercent());
            ps.setInt(5, product.getStockQuantity());
            ps.setString(6, product.getImageUrl());
            ps.setInt(7, product.getCategoryId());
            ps.setBoolean(8, product.isActive());
            ps.setInt(9, product.getProductId());
            ps.setInt(10, product.getSellerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating product: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStock(int productId, int quantityDelta) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ? AND (stock_quantity + ?) >= 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityDelta);
            ps.setInt(2, productId);
            ps.setInt(3, quantityDelta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating product stock: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateApproval(int productId, boolean isApproved) {
        String sql = "UPDATE products SET is_approved = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isApproved);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error toggling product approval: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "UPDATE products SET is_active = FALSE WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error soft-deleting product: " + e.getMessage(), e);
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setSellerId(rs.getInt("seller_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setName(rs.getString("name"));
        p.setSlug(rs.getString("slug"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setDiscountPercent(rs.getInt("discount_percent"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setImageUrl(rs.getString("image_url"));
        p.setRating(rs.getDouble("rating"));
        p.setReviewCount(rs.getInt("review_count"));
        p.setApproved(rs.getBoolean("is_approved"));
        p.setActive(rs.getBoolean("is_active"));

        try {
            p.setCategoryName(rs.getString("category_name"));
        } catch (SQLException ignored) {}
        try {
            p.setSellerName(rs.getString("seller_name"));
        } catch (SQLException ignored) {}

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) p.setUpdatedAt(updated.toLocalDateTime());

        return p;
    }
}
