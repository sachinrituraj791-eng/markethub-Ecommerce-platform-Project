package dao;

import model.Order;
import model.OrderItem;
import model.enums.OrderStatus;
import model.enums.PaymentStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * OrderDAO Interface
 * Defines transactional and query operations for customer orders.
 * Includes ACID transaction workflow for checkout.
 */
public interface OrderDAO extends GenericDAO<Order, Integer> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByBuyerId(int buyerId);

    List<Order> findBySellerId(int sellerId);

    boolean updateStatus(int orderId, OrderStatus status);

    boolean updatePaymentStatus(int orderId, PaymentStatus status);

    /**
     * Executes the complete multi-step ACID Transaction:
     * 1. Check & lock stock
     * 2. Insert order header
     * 3. Insert all line items
     * 4. Deduct stock quantity
     * 5. Clear buyer cart
     */
    Order placeOrderTransactional(int buyerId, String shippingAddress, String paymentMethod) throws Exception;

    BigDecimal calculateTotalRevenue();

    BigDecimal calculateSellerRevenue(int sellerId);

    int countOrders();
}
