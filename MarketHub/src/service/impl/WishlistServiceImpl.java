package service.impl;

import dao.WishlistDAO;
import dao.impl.WishlistDAOImpl;
import model.WishlistItem;
import service.WishlistService;

import java.util.List;

public class WishlistServiceImpl implements WishlistService {

    private final WishlistDAO wishlistDAO;

    public WishlistServiceImpl() {
        this.wishlistDAO = new WishlistDAOImpl();
    }

    public WishlistServiceImpl(WishlistDAO wishlistDAO) {
        this.wishlistDAO = wishlistDAO;
    }

    @Override
    public List<WishlistItem> getUserWishlist(int userId) {
        return wishlistDAO.findByUserId(userId);
    }

    @Override
    public boolean addToWishlist(int userId, int productId) {
        return wishlistDAO.addItem(userId, productId);
    }

    @Override
    public boolean removeFromWishlist(int userId, int productId) {
        return wishlistDAO.removeItem(userId, productId);
    }

    @Override
    public boolean isItemInWishlist(int userId, int productId) {
        return wishlistDAO.isInWishlist(userId, productId);
    }
}
