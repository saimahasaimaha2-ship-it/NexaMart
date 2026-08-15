package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.CartDAO;
import com.nexamart.nexamart.dao.CartDAOImpl;
import com.nexamart.nexamart.dao.OrderDAO;
import com.nexamart.nexamart.dao.OrderDAOImpl;
import com.nexamart.nexamart.dao.ProductDAO;
import com.nexamart.nexamart.dao.ProductDAOImpl;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.listener.DataSourceListener;
import com.nexamart.nexamart.model.CartItem;
import com.nexamart.nexamart.model.Order;
import com.nexamart.nexamart.model.OrderItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final CartDAO cartDAO = new CartDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final OrderDAO orderDAO = new OrderDAOImpl();

    // Places an order from the buyer's current cart via a mock payment confirmation.
    public Order checkout(Long buyerId) throws ServiceException {
        try {
            List<CartItem> cartItems = cartDAO.findByUser(buyerId);
            if (cartItems.isEmpty()) {
                throw new ServiceException("VALIDATION_ERROR", "Cart is empty");
            }

            BigDecimal total = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem ci : cartItems) {
                OrderItem oi = new OrderItem();
                oi.setProductId(ci.getProductId());
                oi.setQuantity(ci.getQuantity());
                oi.setUnitPrice(ci.getUnitPrice());
                orderItems.add(oi);
                total = total.add(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            }

            Order order = new Order();
            order.setBuyerId(buyerId);
            order.setStatus("PENDING"); // mock payment confirmation immediately marks PENDING -> CONFIRMED below
            order.setTotalAmount(total);
            order.setItems(orderItems);

            try (Connection conn = DataSourceListener.getDataSource().getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Mock payment confirmation step (always succeeds in this scope).
                    order.setStatus("CONFIRMED");
                    orderDAO.createOrderWithItems(conn, order);

                    for (OrderItem oi : orderItems) {
                        productDAO.decrementStock(oi.getProductId(), oi.getQuantity());
                    }
                    cartDAO.clearCart(buyerId);
                    conn.commit();
                } catch (Exception inner) {
                    conn.rollback();
                    throw inner;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            return order;
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Checkout failed: " + e.getMessage());
        }
    }

    public List<Order> buyerHistory(Long buyerId) throws ServiceException {
        try {
            return orderDAO.findByBuyer(buyerId);
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not load order history");
        }
    }

    public List<Order> sellerIncomingOrders(Long sellerId) throws ServiceException {
        try {
            return orderDAO.findBySellerProducts(sellerId);
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not load seller orders");
        }
    }
}
