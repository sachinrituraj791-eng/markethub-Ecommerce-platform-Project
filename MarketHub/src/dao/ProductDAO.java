package dao;

import model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductDAO Interface
 * Defines database contracts for marketplace catalog, filtering, pagination, and inventory.
 */
public interface ProductDAO extends GenericDAO<Product, Integer> {

    List<Product> findFiltered(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page, int limit);

    int countFiltered(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findBySellerId(int sellerId);

    List<Product> findFeatured(int limit);

    List<Product> findLowStock(int sellerId, int threshold);

    boolean updateStock(int productId, int quantityDelta);

    boolean updateApproval(int productId, boolean isApproved);
}
