package service;

import model.Cart;

public interface CartService {
    Cart getCart(int userId);
    boolean addItem(int userId, int productId, int quantity);
    boolean updateQuantity(int userId, int productId, int quantity);
    boolean removeItem(int userId, int productId);
    boolean clearCart(int userId);
}
