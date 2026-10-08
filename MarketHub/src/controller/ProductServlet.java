package controller;

import exception.AuthorizationException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Product;
import model.User;
import model.enums.UserRole;
import service.ProductService;
import service.impl.ProductServiceImpl;
import util.JSONUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductServlet
 * RESTful endpoint for catalog browsing, search, and Seller product management.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/api/products", "/api/products/*"})
public class ProductServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        this.productService = new ProductServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                // Paginated product query
                String search = req.getParameter("search");
                String catParam = req.getParameter("category");
                Integer categoryId = (catParam != null && !catParam.isEmpty()) ? Integer.parseInt(catParam) : null;
                String minPriceParam = req.getParameter("minPrice");
                BigDecimal minPrice = (minPriceParam != null && !minPriceParam.isEmpty()) ? new BigDecimal(minPriceParam) : null;
                String maxPriceParam = req.getParameter("maxPrice");
                BigDecimal maxPrice = (maxPriceParam != null && !maxPriceParam.isEmpty()) ? new BigDecimal(maxPriceParam) : null;
                String sort = req.getParameter("sort");
                int page = req.getParameter("page") != null ? Integer.parseInt(req.getParameter("page")) : 1;
                int limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : 12;

                Map<String, Object> result = productService.getProductsWithPagination(search, categoryId, minPrice, maxPrice, sort, page, limit);

                @SuppressWarnings("unchecked")
                List<Product> products = (List<Product>) result.get("products");
                int total = (int) result.get("total");
                int totalPages = (int) result.get("totalPages");

                StringBuilder jsonList = new StringBuilder("[");
                for (int i = 0; i < products.size(); i++) {
                    if (i > 0) jsonList.append(",");
                    jsonList.append(productToJson(products.get(i)));
                }
                jsonList.append("]");

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JSONUtils.paginatedResponse(jsonList.toString(), page, limit, total, totalPages));

            } else {
                // Single product by ID: /api/products/{id}
                int productId = Integer.parseInt(pathInfo.substring(1));
                Product product = productService.getProductById(productId);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JSONUtils.successResponse("Product retrieved", productToJson(product)));
            }
        } catch (ResourceNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse("Invalid product ID or filter format"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Server error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null || (currentUser.getRole() != UserRole.SELLER && currentUser.getRole() != UserRole.ADMIN)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JSONUtils.errorResponse("Only sellers can create products."));
            return;
        }

        try {
            String body = JSONUtils.readBody(req.getReader());
            Map<String, String> data = JSONUtils.parseSimpleJson(body);

            int categoryId = Integer.parseInt(data.get("categoryId"));
            String name = data.get("name");
            String description = data.get("description");
            BigDecimal price = new BigDecimal(data.get("price"));
            int discount = data.get("discountPercent") != null ? Integer.parseInt(data.get("discountPercent")) : 0;
            int stock = Integer.parseInt(data.get("stockQuantity"));
            String imageUrl = data.get("imageUrl");

            Product created = productService.createProduct(currentUser.getUserId(), categoryId, name, description, price, discount, stock, imageUrl);
            resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
            resp.getWriter().write(JSONUtils.successResponse("Product created successfully", productToJson(created)));
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Error creating product: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JSONUtils.errorResponse("Unauthorized"));
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse("Missing product ID in path"));
            return;
        }

        try {
            int productId = Integer.parseInt(pathInfo.substring(1));
            String body = JSONUtils.readBody(req.getReader());
            Map<String, String> data = JSONUtils.parseSimpleJson(body);

            int categoryId = Integer.parseInt(data.get("categoryId"));
            String name = data.get("name");
            String description = data.get("description");
            BigDecimal price = new BigDecimal(data.get("price"));
            int discount = data.get("discountPercent") != null ? Integer.parseInt(data.get("discountPercent")) : 0;
            int stock = Integer.parseInt(data.get("stockQuantity"));
            String imageUrl = data.get("imageUrl");
            boolean active = data.get("isActive") == null || Boolean.parseBoolean(data.get("isActive"));

            boolean updated = productService.updateProduct(currentUser.getUserId(), productId, categoryId, name, description, price, discount, stock, imageUrl, active);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JSONUtils.successResponse("Product updated successfully", "{\"productId\":" + productId + "}"));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JSONUtils.errorResponse("Update failed."));
            }
        } catch (AuthorizationException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Server error: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JSONUtils.errorResponse("Unauthorized"));
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse("Missing product ID in path"));
            return;
        }

        try {
            int productId = Integer.parseInt(pathInfo.substring(1));
            productService.deleteProduct(currentUser.getUserId(), productId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Product deleted successfully", "{\"productId\":" + productId + "}"));
        } catch (AuthorizationException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Error deleting product: " + e.getMessage()));
        }
    }

    public static String productToJson(Product p) {
        return "{\"productId\":" + p.getProductId() +
                ",\"sellerId\":" + p.getSellerId() +
                ",\"categoryId\":" + p.getCategoryId() +
                ",\"name\":\"" + JSONUtils.escape(p.getName()) + "\"" +
                ",\"slug\":\"" + JSONUtils.escape(p.getSlug()) + "\"" +
                ",\"description\":\"" + JSONUtils.escape(p.getDescription()) + "\"" +
                ",\"price\":" + p.getPrice() +
                ",\"discountPercent\":" + p.getDiscountPercent() +
                ",\"discountedPrice\":" + p.getDiscountedPrice() +
                ",\"stockQuantity\":" + p.getStockQuantity() +
                ",\"imageUrl\":\"" + JSONUtils.escape(p.getImageUrl()) + "\"" +
                ",\"rating\":" + p.getRating() +
                ",\"reviewCount\":" + p.getReviewCount() +
                ",\"sellerName\":\"" + JSONUtils.escape(p.getSellerName()) + "\"" +
                ",\"categoryName\":\"" + JSONUtils.escape(p.getCategoryName()) + "\"" +
                ",\"isApproved\":" + p.isApproved() +
                ",\"isActive\":" + p.isActive() + "}";
    }
}
