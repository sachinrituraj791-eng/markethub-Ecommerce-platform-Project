package service.impl;

import dao.ProductDAO;
import dao.impl.ProductDAOImpl;
import exception.AuthorizationException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Product;
import service.ProductService;
import util.ValidationUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;

    public ProductServiceImpl() {
        this.productDAO = new ProductDAOImpl();
    }

    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public Map<String, Object> getProductsWithPagination(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page, int limit) {
        if (page < 1) page = 1;
        if (limit < 1 || limit > 100) limit = 12;

        List<Product> products = productDAO.findFiltered(keyword, categoryId, minPrice, maxPrice, sortBy, page, limit);
        int total = productDAO.countFiltered(keyword, categoryId, minPrice, maxPrice);
        int totalPages = (int) Math.ceil((double) total / limit);

        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("page", page);
        result.put("limit", limit);
        result.put("total", total);
        result.put("totalPages", totalPages);
        return result;
    }

    @Override
    public Product getProductById(int productId) {
        return productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    @Override
    public List<Product> getFeaturedProducts(int limit) {
        return productDAO.findFeatured(limit <= 0 ? 8 : limit);
    }

    @Override
    public List<Product> getSellerProducts(int sellerId) {
        return productDAO.findBySellerId(sellerId);
    }

    @Override
    public List<Product> getSellerLowStock(int sellerId, int threshold) {
        return productDAO.findLowStock(sellerId, threshold <= 0 ? 10 : threshold);
    }

    @Override
    public Product createProduct(int sellerId, int categoryId, String name, String description, BigDecimal price, int discountPercent, int stock, String imageUrl) {
        ValidationUtils.validateNotBlank(name, "Product Name");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Product price must be non-negative.");
        }
        if (stock < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }

        Product p = new Product();
        p.setSellerId(sellerId);
        p.setCategoryId(categoryId);
        p.setName(name.trim());
        p.setSlug(name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        p.setDescription(description);
        p.setPrice(price);
        p.setDiscountPercent(Math.max(0, Math.min(100, discountPercent)));
        p.setStockQuantity(stock);
        p.setImageUrl(imageUrl != null && !imageUrl.isEmpty() ? imageUrl : "https://picsum.photos/seed/" + p.getSlug() + "/600/600");
        p.setApproved(true);
        p.setActive(true);

        return productDAO.save(p);
    }

    @Override
    public boolean updateProduct(int sellerId, int productId, int categoryId, String name, String description, BigDecimal price, int discountPercent, int stock, String imageUrl, boolean isActive) {
        Product existing = getProductById(productId);
        // Authorization: Sellers cannot modify another seller's products
        if (existing.getSellerId() != sellerId) {
            throw new AuthorizationException("Access denied: You can only modify your own products.");
        }

        ValidationUtils.validateNotBlank(name, "Product Name");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Product price must be non-negative.");
        }
        if (stock < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }

        existing.setCategoryId(categoryId);
        existing.setName(name.trim());
        existing.setDescription(description);
        existing.setPrice(price);
        existing.setDiscountPercent(Math.max(0, Math.min(100, discountPercent)));
        existing.setStockQuantity(stock);
        if (imageUrl != null && !imageUrl.isEmpty()) existing.setImageUrl(imageUrl);
        existing.setActive(isActive);

        return productDAO.update(existing);
    }

    @Override
    public boolean deleteProduct(int sellerId, int productId) {
        Product existing = getProductById(productId);
        if (existing.getSellerId() != sellerId) {
            throw new AuthorizationException("Access denied: You cannot delete another seller's product.");
        }
        return productDAO.deleteById(productId);
    }

    @Override
    public boolean updateStock(int productId, int delta) {
        return productDAO.updateStock(productId, delta);
    }

    @Override
    public boolean approveProduct(int adminId, int productId, boolean approved) {
        return productDAO.updateApproval(productId, approved);
    }
}
