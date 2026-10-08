package service;

import model.Review;
import java.util.List;

public interface ReviewService {
    List<Review> getReviewsForProduct(int productId);
    Review addReview(int userId, int productId, int rating, String comment);
}
