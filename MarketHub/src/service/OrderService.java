package service;

import model.Order;
import model.enums.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Order placeOrder(int buyerId, String shippingAddress, String paymentMethod) throws Exception;
    Order getOrderById(int orderId, int requestingUserId, boolean isAdmin);
    List<Order> getBuyerOrders(int buyerId);
    List<Order> getSellerOrders(int sellerId);
    List<Order> getAllOrders();
    boolean updateOrderStatus(int sellerOrAdminId, int orderId, OrderStatus newStatus, boolean isAdmin);
    Map<String, Object> getSellerDashboardStats(int sellerId);
    Map<String, Object> getAdminDashboardStats();
}
