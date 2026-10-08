package dao;

import model.Review;
import java.util.List;

/**
 * ReviewDAO Interface
 */
public interface ReviewDAO extends GenericDAO<Review, Integer> {

    List<Review> findByProductId(int productId);

    boolean hasUserReviewedProduct(int userId, int productId);

    double calculateAverageRating(int productId);
}
