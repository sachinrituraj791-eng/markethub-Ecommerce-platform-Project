package service.impl;

import dao.ReviewDAO;
import dao.impl.ReviewDAOImpl;
import exception.ValidationException;
import model.Review;
import service.ReviewService;
import util.ValidationUtils;

import java.util.List;

public class ReviewServiceImpl implements ReviewService {

    private final ReviewDAO reviewDAO;

    public ReviewServiceImpl() {
        this.reviewDAO = new ReviewDAOImpl();
    }

    public ReviewServiceImpl(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    @Override
    public List<Review> getReviewsForProduct(int productId) {
        return reviewDAO.findByProductId(productId);
    }

    @Override
    public Review addReview(int userId, int productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5 stars.");
        }
        ValidationUtils.validateNotBlank(comment, "Review Comment");

        if (reviewDAO.hasUserReviewedProduct(userId, productId)) {
            throw new ValidationException("You have already reviewed this product.");
        }

        Review r = new Review(0, productId, userId, rating, comment.trim());
        return reviewDAO.save(r);
    }
}
