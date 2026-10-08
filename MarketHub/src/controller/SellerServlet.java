package controller;

import exception.AuthorizationException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Order;
import model.Product;
import model.User;
import model.enums.OrderStatus;
import model.enums.UserRole;
import service.OrderService;
import service.ProductService;
import service.impl.OrderServiceImpl;
import service.impl.ProductServiceImpl;
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
 * SellerServlet
 * Handles merchant inventory management, sales tracking, and order fulfillment.
 */
@WebServlet(name = "SellerServlet", urlPatterns = {
        "/api/seller/products",
        "/api/seller/orders",
        "/api/seller/orders/*",
        "/api/seller/dashboard"
})
public class SellerServlet extends HttpServlet {

    private ProductService productService;
    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        this.productService = new ProductServiceImpl();
        this.orderService = new OrderServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User seller = getAuthenticatedSeller(req, resp);
        if (seller == null) return;

        String uri = req.getRequestURI();

        if (uri.endsWith("/products")) {
            List<Product> products = productService.getSellerProducts(seller.getUserId());
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < products.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(ProductServlet.productToJson(products.get(i)));
            }
            sb.append("]");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Seller products retrieved", sb.toString()));

        } else if (uri.endsWith("/orders")) {
            List<Order> orders = orderService.getSellerOrders(seller.getUserId());
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(OrderServlet.orderToJson(orders.get(i)));
            }
            sb.append("]");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Seller orders retrieved", sb.toString()));

        } else if (uri.endsWith("/dashboard")) {
            Map<String, Object> stats = orderService.getSellerDashboardStats(seller.getUserId());
            String statsJson = "{\"totalOrders\":" + stats.get("totalOrders") +
                    ",\"totalRevenue\":" + stats.get("totalRevenue") +
                    ",\"totalProducts\":" + stats.get("totalProducts") +
                    ",\"lowStockCount\":" + stats.get("lowStockCount") + "}";
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Dashboard metrics retrieved", statsJson));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User seller = getAuthenticatedSeller(req, resp);
        if (seller == null) return;

        String pathInfo = req.getPathInfo(); // e.g. /{id}/status
        if (pathInfo != null && pathInfo.contains("/status")) {
            try {
                String[] parts = pathInfo.split("/");
                int orderId = Integer.parseInt(parts[1]);

                String body = JSONUtils.readBody(req.getReader());
                Map<String, String> data = JSONUtils.parseSimpleJson(body);
                String statusStr = data.get("status");
                OrderStatus newStatus = OrderStatus.fromString(statusStr);

                boolean updated = orderService.updateOrderStatus(seller.getUserId(), orderId, newStatus, seller.getRole() == UserRole.ADMIN);
                if (updated) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(JSONUtils.successResponse("Order status updated to " + newStatus.name(), "{\"orderId\":" + orderId + "}"));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(JSONUtils.errorResponse("Failed to update status"));
                }
            } catch (AuthorizationException e) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
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

    private User getAuthenticatedSeller(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JSONUtils.errorResponse("Authentication required"));
            return null;
        }
        User user = (User) session.getAttribute("currentUser");
        if (user.getRole() != UserRole.SELLER && user.getRole() != UserRole.ADMIN) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JSONUtils.errorResponse("Seller privilege required"));
            return null;
        }
        return user;
    }
}
