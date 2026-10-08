package service.impl;

import dao.CartDAO;
import dao.ProductDAO;
import dao.impl.CartDAOImpl;
import dao.impl.ProductDAOImpl;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Cart;
import model.Product;
import service.CartService;

public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartServiceImpl() {
        this.cartDAO = new CartDAOImpl();
        this.productDAO = new ProductDAOImpl();
    }

    public CartServiceImpl(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    @Override
    public Cart getCart(int userId) {
        return cartDAO.findByUserId(userId).orElseGet(() -> cartDAO.createCartForUser(userId));
    }

    @Override
    public boolean addItem(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be at least 1.");
        }
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        if (!product.isActive() || !product.isApproved()) {
            throw new ValidationException("Product is not currently available for purchase.");
        }
        if (product.getStockQuantity() < quantity) {
            throw new ValidationException("Requested quantity exceeds available stock (" + product.getStockQuantity() + ").");
        }

        return cartDAO.addItem(userId, productId, quantity);
    }

    @Override
    public boolean updateQuantity(int userId, int productId, int quantity) {
        if (quantity < 0) {
            throw new ValidationException("Quantity cannot be negative.");
        }
        if (quantity > 0) {
            Product product = productDAO.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
            if (product.getStockQuantity() < quantity) {
                throw new ValidationException("Requested quantity exceeds available stock (" + product.getStockQuantity() + ").");
            }
        }
        return cartDAO.updateItemQuantity(userId, productId, quantity);
    }

    @Override
    public boolean removeItem(int userId, int productId) {
        return cartDAO.removeItem(userId, productId);
    }

    @Override
    public boolean clearCart(int userId) {
        return cartDAO.clearCart(userId);
    }
}
