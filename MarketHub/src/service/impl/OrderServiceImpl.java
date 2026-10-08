package service.impl;

import dao.OrderDAO;
import dao.ProductDAO;
import dao.UserDAO;
import dao.impl.OrderDAOImpl;
import dao.impl.ProductDAOImpl;
import dao.impl.UserDAOImpl;
import exception.AuthorizationException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Order;
import model.OrderItem;
import model.enums.OrderStatus;
import model.enums.UserRole;
import service.OrderService;
import util.ValidationUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;

    public OrderServiceImpl() {
        this.orderDAO = new OrderDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    public OrderServiceImpl(OrderDAO orderDAO, ProductDAO productDAO, UserDAO userDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Order placeOrder(int buyerId, String shippingAddress, String paymentMethod) throws Exception {
        ValidationUtils.validateNotBlank(shippingAddress, "Shipping Address");
        return orderDAO.placeOrderTransactional(buyerId, shippingAddress.trim(), paymentMethod);
    }

    @Override
    public Order getOrderById(int orderId, int requestingUserId, boolean isAdmin) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!isAdmin && order.getBuyerId() != requestingUserId) {
            // Check if user is a seller for items in this order
            boolean isSellerOfItem = order.getItems() != null &&
                    order.getItems().stream().anyMatch(item -> item.getSellerId() == requestingUserId);
            if (!isSellerOfItem) {
                throw new AuthorizationException("Access denied: You are not authorized to view this order.");
            }
        }
        return order;
    }

    @Override
    public List<Order> getBuyerOrders(int buyerId) {
        return orderDAO.findByBuyerId(buyerId);
    }

    @Override
    public List<Order> getSellerOrders(int sellerId) {
        return orderDAO.findBySellerId(sellerId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    @Override
    public boolean updateOrderStatus(int sellerOrAdminId, int orderId, OrderStatus newStatus, boolean isAdmin) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!isAdmin) {
            boolean isSellerOfItem = order.getItems() != null &&
                    order.getItems().stream().anyMatch(item -> item.getSellerId() == sellerOrAdminId);
            if (!isSellerOfItem) {
                throw new AuthorizationException("Access denied: You can only update orders containing your products.");
            }
        }

        if (!order.getOrderStatus().canTransitionTo(newStatus)) {
            throw new ValidationException("Cannot transition order from " + order.getOrderStatus() + " to " + newStatus);
        }

        return orderDAO.updateStatus(orderId, newStatus);
    }

    @Override
    public Map<String, Object> getSellerDashboardStats(int sellerId) {
        List<Order> sellerOrders = orderDAO.findBySellerId(sellerId);
        BigDecimal revenue = orderDAO.calculateSellerRevenue(sellerId);
        int lowStockCount = productDAO.findLowStock(sellerId, 10).size();
        int totalProducts = productDAO.findBySellerId(sellerId).size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", sellerOrders.size());
        stats.put("totalRevenue", revenue);
        stats.put("totalProducts", totalProducts);
        stats.put("lowStockCount", lowStockCount);
        return stats;
    }

    @Override
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userDAO.countUsers());
        stats.put("totalBuyers", userDAO.countByRole(UserRole.BUYER));
        stats.put("totalSellers", userDAO.countByRole(UserRole.SELLER));
        stats.put("totalOrders", orderDAO.countOrders());
        stats.put("totalRevenue", orderDAO.calculateTotalRevenue());
        return stats;
    }
}
