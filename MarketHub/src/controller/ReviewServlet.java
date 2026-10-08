package controller;

import exception.ValidationException;
import model.Review;
import model.User;
import service.ReviewService;
import service.impl.ReviewServiceImpl;
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
 * ReviewServlet
 * Endpoints:
 * GET  /api/reviews/{productId}
 * POST /api/reviews
 */
@WebServlet(name = "ReviewServlet", urlPatterns = {"/api/reviews", "/api/reviews/*"})
public class ReviewServlet extends HttpServlet {

    private ReviewService reviewService;

    @Override
    public void init() throws ServletException {
        this.reviewService = new ReviewServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse("Product ID is required in URL path"));
            return;
        }

        try {
            int productId = Integer.parseInt(pathInfo.substring(1));
            List<Review> reviews = reviewService.getReviewsForProduct(productId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < reviews.size(); i++) {
                if (i > 0) sb.append(",");
                Review r = reviews.get(i);
                sb.append("{\"reviewId\":").append(r.getReviewId())
                        .append(",\"productId\":").append(r.getProductId())
                        .append(",\"userId\":").append(r.getUserId())
                        .append(",\"userName\":\"").append(JSONUtils.escape(r.getUserName()))
                        .append("\",\"rating\":").append(r.getRating())
                        .append(",\"comment\":\"").append(JSONUtils.escape(r.getComment()))
                        .append("\",\"createdAt\":\"").append(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "")
                        .append("\"}");
            }
            sb.append("]");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Reviews retrieved", sb.toString()));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse("Invalid product ID"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JSONUtils.errorResponse("Authentication required to write a review."));
            return;
        }

        try {
            String body = JSONUtils.readBody(req.getReader());
            Map<String, String> data = JSONUtils.parseSimpleJson(body);

            int productId = Integer.parseInt(data.get("productId"));
            int rating = Integer.parseInt(data.get("rating"));
            String comment = data.get("comment");

            Review saved = reviewService.addReview(currentUser.getUserId(), productId, rating, comment);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JSONUtils.successResponse("Review submitted successfully",
                    "{\"reviewId\":" + saved.getReviewId() + ",\"rating\":" + saved.getRating() + "}"));
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Error saving review: " + e.getMessage()));
        }
    }
}
