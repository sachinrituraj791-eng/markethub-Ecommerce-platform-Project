package dao;

import model.WishlistItem;
import java.util.List;

/**
 * WishlistDAO Interface
 */
public interface WishlistDAO {

    List<WishlistItem> findByUserId(int userId);

    boolean addItem(int userId, int productId);

    boolean removeItem(int userId, int productId);

    boolean isInWishlist(int userId, int productId);
}
