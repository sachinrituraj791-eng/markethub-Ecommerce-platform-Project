package service;

import model.WishlistItem;
import java.util.List;

public interface WishlistService {
    List<WishlistItem> getUserWishlist(int userId);
    boolean addToWishlist(int userId, int productId);
    boolean removeFromWishlist(int userId, int productId);
    boolean isItemInWishlist(int userId, int productId);
}
