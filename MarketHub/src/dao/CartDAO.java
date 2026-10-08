package dao;

import model.Cart;
import model.CartItem;
import java.util.Optional;

/**
 * CartDAO Interface
 * Defines contracts for shopping cart persistence and item management.
 */
public interface CartDAO {

    Optional<Cart> findByUserId(int userId);

    Cart createCartForUser(int userId);

    boolean addItem(int userId, int productId, int quantity);

    boolean updateItemQuantity(int userId, int productId, int newQuantity);

    boolean removeItem(int userId, int productId);

    boolean clearCart(int userId);
}
