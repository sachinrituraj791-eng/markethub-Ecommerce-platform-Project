package dao;

import model.Category;
import java.util.Optional;

/**
 * CategoryDAO Interface
 */
public interface CategoryDAO extends GenericDAO<Category, Integer> {
    Optional<Category> findBySlug(String slug);
}
