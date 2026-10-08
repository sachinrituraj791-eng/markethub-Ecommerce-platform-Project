package controller;

import exception.AuthorizationException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Order;
import model.Product;
import model.User;
import service.OrderService;
import service.ProductService;
import service.UserService;
import service.impl.OrderServiceImpl;
import service.impl.ProductServiceImpl;
import service.impl.UserServiceImpl;
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
 * AdminServlet
 * Platform administration endpoints for users, catalog review, orders, and KPIs.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {
        "/api/admin/users",
        "/api/admin/users/*",
        "/api/admin/products",
        "/api/admin/orders",
        "/api/admin/dashboard"
})
public class AdminServlet extends HttpServlet {

    private UserService userService;
    private ProductService productService;
    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        this.userService = new UserServiceImpl();
        this.productService = new ProductServiceImpl();
        this.orderService = new OrderServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User admin = getAuthenticatedAdmin(req, resp);
        if (admin == null) return;

        String uri = req.getRequestURI();

        if (uri.endsWith("/users")) {
            List<User> users = userService.getAllUsers();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                if (i > 0) sb.append(",");
                User u = users.get(i);
                sb.append("{\"userId\":").append(u.getUserId())
                        .append(",\"username\":\"").append(JSONUtils.escape(u.getUsername()))
                        .append("\",\"email\":\"").append(JSONUtils.escape(u.getEmail()))
                        .append("\",\"fullName\":\"").append(JSONUtils.escape(u.getFullName()))
                        .append("\",\"role\":\"").append(u.getRole().name())
                        .append("\",\"phone\":\"").append(JSONUtils.escape(u.getPhone()))
                        .append("\",\"address\":\"").append(JSONUtils.escape(u.getAddress()))
                        .append("\",\"isActive\":").append(u.isActive())
                        .append(",\"createdAt\":\"").append(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "")
                        .append("\"}");
            }
            sb.append("]");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("All users retrieved", sb.toString()));

        } else if (uri.endsWith("/products")) {
            // Admin views all products across sellers
            Map<String, Object> res = productService.getProductsWithPagination(null, null, null, null, "newest", 1, 100);
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) res.get("products");
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < products.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(ProductServlet.productToJson(products.get(i)));
            }
            sb.append("]");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("All products retrieved", sb.toString()));

        } else if (uri.endsWith("/orders")) {
            List<Order> orders = orderService.getAllOrders();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(OrderServlet.orderToJson(orders.get(i)));
            }
            sb.append("]");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("All orders retrieved", sb.toString()));

        } else if (uri.endsWith("/dashboard")) {
            Map<String, Object> stats = orderService.getAdminDashboardStats();
            String statsJson = "{\"totalUsers\":" + stats.get("totalUsers") +
                    ",\"totalBuyers\":" + stats.get("totalBuyers") +
                    ",\"totalSellers\":" + stats.get("totalSellers") +
                    ",\"totalOrders\":" + stats.get("totalOrders") +
                    ",\"totalRevenue\":" + stats.get("totalRevenue") + "}";
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Admin dashboard KPIs retrieved", statsJson));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User admin = getAuthenticatedAdmin(req, resp);
        if (admin == null) return;

        String pathInfo = req.getPathInfo(); // e.g. /{userId}/status
        if (pathInfo != null && pathInfo.contains("/status")) {
            try {
                String[] parts = pathInfo.split("/");
                int targetUserId = Integer.parseInt(parts[1]);

                String body = JSONUtils.readBody(req.getReader());
                Map<String, String> data = JSONUtils.parseSimpleJson(body);
                boolean isActive = Boolean.parseBoolean(data.get("isActive"));

                userService.setUserActiveStatus(admin.getUserId(), targetUserId, isActive);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JSONUtils.successResponse("User status updated", "{\"userId\":" + targetUserId + ",\"isActive\":" + isActive + "}"));
            } catch (ValidationException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
            } catch (ResourceNotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(JSONUtils.errorResponse("Server error: " + e.getMessage()));
            }
        }
    }

    private User getAuthenticatedAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JSONUtils.errorResponse("Authentication required"));
            return null;
        }
        User user = (User) session.getAttribute("currentUser");
        if (user.getRole() != model.enums.UserRole.ADMIN) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JSONUtils.errorResponse("Administrator privileges required"));
            return null;
        }
        return user;
    }
}
