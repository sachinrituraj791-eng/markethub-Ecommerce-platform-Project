package service;

import model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService {
    Map<String, Object> getProductsWithPagination(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page, int limit);
    Product getProductById(int productId);
    List<Product> getFeaturedProducts(int limit);
    List<Product> getSellerProducts(int sellerId);
    List<Product> getSellerLowStock(int sellerId, int threshold);
    Product createProduct(int sellerId, int categoryId, String name, String description, BigDecimal price, int discountPercent, int stock, String imageUrl);
    boolean updateProduct(int sellerId, int productId, int categoryId, String name, String description, BigDecimal price, int discountPercent, int stock, String imageUrl, boolean isActive);
    boolean deleteProduct(int sellerId, int productId);
    boolean updateStock(int productId, int delta);
    boolean approveProduct(int adminId, int productId, boolean approved);
}
