package controller;

import exception.AuthorizationException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Order;
import model.OrderItem;
import model.User;
import model.enums.UserRole;
import service.OrderService;
import service.impl.OrderServiceImpl;
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
 * OrderServlet
 * Manages order creation with transactional integrity and order queries.
 */
@WebServlet(name = "OrderServlet", urlPatterns = {"/api/orders", "/api/orders/*"})
public class OrderServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        this.orderService = new OrderServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                // List buyer's orders
                List<Order> orders = orderService.getBuyerOrders(currentUser.getUserId());
                StringBuilder jsonList = new StringBuilder("[");
                for (int i = 0; i < orders.size(); i++) {
                    if (i > 0) jsonList.append(",");
                    jsonList.append(orderToJson(orders.get(i)));
                }
                jsonList.append("]");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JSONUtils.successResponse("Orders retrieved", jsonList.toString()));
            } else {
                int orderId = Integer.parseInt(pathInfo.substring(1));
                boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
                Order order = orderService.getOrderById(orderId, currentUser.getUserId(), isAdmin);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JSONUtils.successResponse("Order retrieved", orderToJson(order)));
            }
        } catch (AuthorizationException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        try {
            String body = JSONUtils.readBody(req.getReader());
            Map<String, String> data = JSONUtils.parseSimpleJson(body);

            String shippingAddress = data.get("shippingAddress");
            String paymentMethod = data.get("paymentMethod");

            // Execute real JDBC transaction
            Order createdOrder = orderService.placeOrder(currentUser.getUserId(), shippingAddress, paymentMethod);

            resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
            resp.getWriter().write(JSONUtils.successResponse("Order placed successfully with ACID transaction guarantee.", orderToJson(createdOrder)));
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Transaction failed and was rolled back: " + e.getMessage()));
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

    public static String orderToJson(Order o) {
        StringBuilder itemsJson = new StringBuilder("[");
        if (o.getItems() != null) {
            for (int i = 0; i < o.getItems().size(); i++) {
                if (i > 0) itemsJson.append(",");
                OrderItem item = o.getItems().get(i);
                itemsJson.append("{\"orderItemId\":").append(item.getOrderItemId())
                        .append(",\"productId\":").append(item.getProductId())
                        .append(",\"sellerId\":").append(item.getSellerId())
                        .append(",\"productName\":\"").append(JSONUtils.escape(item.getProductName()))
                        .append("\",\"productImageUrl\":\"").append(JSONUtils.escape(item.getProductImageUrl()))
                        .append("\",\"quantity\":").append(item.getQuantity())
                        .append(",\"unitPrice\":").append(item.getUnitPrice())
                        .append(",\"subtotalPrice\":").append(item.getSubtotalPrice())
                        .append("}");
            }
        }
        itemsJson.append("]");

        return "{\"orderId\":" + o.getOrderId() +
                ",\"orderNumber\":\"" + JSONUtils.escape(o.getOrderNumber()) + "\"" +
                ",\"buyerId\":" + o.getBuyerId() +
                ",\"buyerName\":\"" + JSONUtils.escape(o.getBuyerName()) + "\"" +
                ",\"totalAmount\":" + o.getTotalAmount() +
                ",\"shippingAddress\":\"" + JSONUtils.escape(o.getShippingAddress()) + "\"" +
                ",\"paymentMethod\":\"" + JSONUtils.escape(o.getPaymentMethod()) + "\"" +
                ",\"orderStatus\":\"" + o.getOrderStatus().name() + "\"" +
                ",\"paymentStatus\":\"" + o.getPaymentStatus().name() + "\"" +
                ",\"createdAt\":\"" + (o.getCreatedAt() != null ? o.getCreatedAt().toString() : "") + "\"" +
                ",\"items\":" + itemsJson.toString() + "}";
    }
}
