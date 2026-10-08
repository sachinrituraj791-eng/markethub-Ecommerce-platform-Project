package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WishlistItem Entity
 * Maps user interest to products.
 */
public class WishlistItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int wishlistId;
    private int userId;
    private int productId;
    private LocalDateTime addedAt;

    // Joined presentation data
    private Product product;

    public WishlistItem() {}

    public WishlistItem(int wishlistId, int userId, int productId) {
        this.wishlistId = wishlistId;
        this.userId = userId;
        this.productId = productId;
    }

    public int getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(int wishlistId) {
        this.wishlistId = wishlistId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
