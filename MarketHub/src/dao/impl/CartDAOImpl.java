package dao.impl;

import dao.CartDAO;
import exception.DatabaseException;
import model.Cart;
import model.CartItem;
import model.Product;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CartDAOImpl
 * JDBC Implementation of CartDAO.
 * Manages buyer shopping sessions, line items, and product price mappings.
 */
public class CartDAOImpl implements CartDAO {

    @Override
    public Optional<Cart> findByUserId(int userId) {
        String cartSql = "SELECT cart_id, user_id, updated_at FROM cart WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(cartSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cart cart = new Cart();
                    cart.setCartId(rs.getInt("cart_id"));
                    cart.setUserId(rs.getInt("user_id"));
                    Timestamp updated = rs.getTimestamp("updated_at");
                    if (updated != null) cart.setUpdatedAt(updated.toLocalDateTime());

                    cart.setItems(loadCartItems(cart.getCartId(), conn));
                    return Optional.of(cart);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding cart for user: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<CartItem> loadCartItems(int cartId, Connection conn) throws SQLException {
        String itemSql = "SELECT ci.*, p.name AS product_name, p.image_url, p.price, p.discount_percent, p.stock_quantity " +
                         "FROM cart_items ci " +
                         "JOIN products p ON ci.product_id = p.product_id " +
                         "WHERE ci.cart_id = ? ORDER BY ci.cart_item_id ASC";
        List<CartItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getInt("cart_item_id"));
                    item.setCartId(rs.getInt("cart_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) item.setCreatedAt(created.toLocalDateTime());

                    Product p = new Product();
                    p.setProductId(item.getProductId());
                    p.setName(rs.getString("product_name"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setDiscountPercent(rs.getInt("discount_percent"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    item.setProduct(p);

                    items.add(item);
                }
            }
        }
        return items;
    }

    @Override
    public Cart createCartForUser(int userId) {
        String sql = "INSERT INTO cart (user_id) VALUES (?) ON DUPLICATE KEY UPDATE cart_id = LAST_INSERT_ID(cart_id)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Cart cart = new Cart();
                    cart.setCartId(rs.getInt(1));
                    cart.setUserId(userId);
                    return cart;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating cart: " + e.getMessage(), e);
        }
        return findByUserId(userId).orElse(new Cart(0, userId));
    }

    @Override
    public boolean addItem(int userId, int productId, int quantity) {
        Cart cart = findByUserId(userId).orElseGet(() -> createCartForUser(userId));

        String priceSql = "SELECT price, discount_percent FROM products WHERE product_id = ?";
        String upsertSql = "INSERT INTO cart_items (cart_id, product_id, quantity, unit_price) " +
                           "VALUES (?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";

        try (Connection conn = DBConnection.getConnection()) {
            java.math.BigDecimal unitPrice = java.math.BigDecimal.ZERO;
            try (PreparedStatement psPrice = conn.prepareStatement(priceSql)) {
                psPrice.setInt(1, productId);
                try (ResultSet rs = psPrice.executeQuery()) {
                    if (rs.next()) {
                        java.math.BigDecimal basePrice = rs.getBigDecimal("price");
                        int discount = rs.getInt("discount_percent");
                        if (discount > 0) {
                            java.math.BigDecimal discountFactor = java.math.BigDecimal.valueOf(100 - discount)
                                    .divide(java.math.BigDecimal.valueOf(100), 2, java.math.BigDecimal.ROUND_HALF_UP);
                            unitPrice = basePrice.multiply(discountFactor).setScale(2, java.math.BigDecimal.ROUND_HALF_UP);
                        } else {
                            unitPrice = basePrice;
                        }
                    }
                }
            }

            try (PreparedStatement psUpsert = conn.prepareStatement(upsertSql)) {
                psUpsert.setInt(1, cart.getCartId());
                psUpsert.setInt(2, productId);
                psUpsert.setInt(3, quantity);
                psUpsert.setBigDecimal(4, unitPrice);
                return psUpsert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error adding item to cart: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateItemQuantity(int userId, int productId, int newQuantity) {
        Optional<Cart> cartOpt = findByUserId(userId);
        if (!cartOpt.isPresent()) return false;
        int cartId = cartOpt.get().getCartId();

        if (newQuantity <= 0) {
            return removeItem(userId, productId);
        }

        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, cartId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating cart quantity: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeItem(int userId, int productId) {
        Optional<Cart> cartOpt = findByUserId(userId);
        if (!cartOpt.isPresent()) return false;
        int cartId = cartOpt.get().getCartId();

        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error removing item from cart: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean clearCart(int userId) {
        Optional<Cart> cartOpt = findByUserId(userId);
        if (!cartOpt.isPresent()) return false;
        int cartId = cartOpt.get().getCartId();

        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error clearing cart: " + e.getMessage(), e);
        }
    }
}
