package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Review Entity
 * Customer feedback for products with star ratings and comments.
 */
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private int reviewId;
    private int productId;
    private int userId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    // Joined presentation data
    private String userName;

    public Review() {
        this.rating = 5;
    }

    public Review(int reviewId, int productId, int userId, int rating, String comment) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
