package controller;

import exception.ValidationException;
import model.User;
import model.WishlistItem;
import service.WishlistService;
import service.impl.WishlistServiceImpl;
import util.JSONUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * WishlistServlet
 * Endpoints:
 * GET    /api/wishlist
 * POST   /api/wishlist
 * DELETE /api/wishlist/{productId}
 */
@WebServlet(name = "WishlistServlet", urlPatterns = {"/api/wishlist", "/api/wishlist/*"})
public class WishlistServlet extends HttpServlet {

    private WishlistService wishlistService;

    @Override
    public void init() throws ServletException {
        this.wishlistService = new WishlistServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        List<WishlistItem> items = wishlistService.getUserWishlist(currentUser.getUserId());
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            WishlistItem w = items.get(i);
            sb.append("{\"wishlistId\":").append(w.getWishlistId())
                    .append(",\"productId\":").append(w.getProductId())
                    .append(",\"productName\":\"").append(JSONUtils.escape(w.getProduct() != null ? w.getProduct().getName() : ""))
                    .append("\",\"productImageUrl\":\"").append(JSONUtils.escape(w.getProduct() != null ? w.getProduct().getImageUrl() : ""))
                    .append("\",\"price\":").append(w.getProduct() != null ? w.getProduct().getPrice() : 0)
                    .append(",\"stockQuantity\":").append(w.getProduct() != null ? w.getProduct().getStockQuantity() : 0)
                    .append("}");
        }
        sb.append("]");

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(JSONUtils.successResponse("Wishlist retrieved", sb.toString()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        try {
            String body = JSONUtils.readBody(req.getReader());
            Map<String, String> data = JSONUtils.parseSimpleJson(body);
            int productId = Integer.parseInt(data.get("productId"));

            wishlistService.addToWishlist(currentUser.getUserId(), productId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Added to wishlist", "{\"productId\":" + productId + "}"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Error: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            int productId = Integer.parseInt(pathInfo.substring(1));
            wishlistService.removeFromWishlist(currentUser.getUserId(), productId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Removed from wishlist", "{\"productId\":" + productId + "}"));
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse("Product ID required"));
        }
    }

    private User getAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JSONUtils.errorResponse("Authentication required"));
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }
}
