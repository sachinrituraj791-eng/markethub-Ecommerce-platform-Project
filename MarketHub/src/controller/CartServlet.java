package controller;

import exception.ValidationException;
import model.Cart;
import model.CartItem;
import model.User;
import service.CartService;
import service.impl.CartServiceImpl;
import util.JSONUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * CartServlet
 * Manages buyer shopping cart operations.
 * GET    /api/cart
 * POST   /api/cart (add item)
 * PUT    /api/cart (update quantity)
 * DELETE /api/cart (remove item or clear)
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/api/cart"})
public class CartServlet extends HttpServlet {

    private CartService cartService;

    @Override
    public void init() throws ServletException {
        this.cartService = new CartServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        Cart cart = cartService.getCart(currentUser.getUserId());
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(JSONUtils.successResponse("Cart retrieved", cartToJson(cart)));
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
            int quantity = data.get("quantity") != null ? Integer.parseInt(data.get("quantity")) : 1;

            cartService.addItem(currentUser.getUserId(), productId, quantity);
            Cart updated = cartService.getCart(currentUser.getUserId());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Item added to cart", cartToJson(updated)));
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Error adding item to cart: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        try {
            String body = JSONUtils.readBody(req.getReader());
            Map<String, String> data = JSONUtils.parseSimpleJson(body);

            int productId = Integer.parseInt(data.get("productId"));
            int quantity = Integer.parseInt(data.get("quantity"));

            cartService.updateQuantity(currentUser.getUserId(), productId, quantity);
            Cart updated = cartService.getCart(currentUser.getUserId());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JSONUtils.successResponse("Cart updated", cartToJson(updated)));
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JSONUtils.errorResponse("Error updating cart: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        String productIdParam = req.getParameter("productId");
        if (productIdParam != null && !productIdParam.isEmpty()) {
            int productId = Integer.parseInt(productIdParam);
            cartService.removeItem(currentUser.getUserId(), productId);
        } else {
            cartService.clearCart(currentUser.getUserId());
        }

        Cart updated = cartService.getCart(currentUser.getUserId());
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(JSONUtils.successResponse("Cart updated", cartToJson(updated)));
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

    private String cartToJson(Cart c) {
        StringBuilder itemsJson = new StringBuilder("[");
        if (c.getItems() != null) {
            for (int i = 0; i < c.getItems().size(); i++) {
                if (i > 0) itemsJson.append(",");
                CartItem item = c.getItems().get(i);
                itemsJson.append("{\"cartItemId\":").append(item.getCartItemId())
                        .append(",\"productId\":").append(item.getProductId())
                        .append(",\"quantity\":").append(item.getQuantity())
                        .append(",\"unitPrice\":").append(item.getUnitPrice())
                        .append(",\"subtotal\":").append(item.getSubtotal())
                        .append(",\"productName\":\"").append(JSONUtils.escape(item.getProduct() != null ? item.getProduct().getName() : ""))
                        .append("\",\"productImageUrl\":\"").append(JSONUtils.escape(item.getProduct() != null ? item.getProduct().getImageUrl() : ""))
                        .append("\",\"stockQuantity\":").append(item.getProduct() != null ? item.getProduct().getStockQuantity() : 0)
                        .append("}");
            }
        }
        itemsJson.append("]");

        return "{\"cartId\":" + c.getCartId() +
                ",\"userId\":" + c.getUserId() +
                ",\"itemCount\":" + c.getTotalItemCount() +
                ",\"totalAmount\":" + c.getTotalAmount() +
                ",\"items\":" + itemsJson.toString() + "}";
    }
}
