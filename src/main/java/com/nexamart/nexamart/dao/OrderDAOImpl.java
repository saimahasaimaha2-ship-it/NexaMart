package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.model.Order;
import com.nexamart.nexamart.model.OrderItem;
import com.nexamart.nexamart.listener.DataSourceListener;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {

    // Caller (OrderService) owns the transaction/connection so order + items commit atomically.
    @Override
    public Order createOrderWithItems(Connection conn, Order order) throws Exception {
        String orderSql = "INSERT INTO orders (buyer_id, status, total_amount) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getBuyerId());
            ps.setString(2, order.getStatus());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) order.setId(keys.getLong(1));
            }
        }

        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
            for (OrderItem item : order.getItems()) {
                ps.setLong(1, order.getId());
                ps.setLong(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return order;
    }

    @Override
    public List<Order> findByBuyer(Long buyerId) throws Exception {
        String sql = "SELECT * FROM orders WHERE buyer_id = ? ORDER BY created_at DESC";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) orders.add(mapOrder(rs));
                for (Order o : orders) { o.setItems(findItemsByOrderId(o.getId())); }
                return orders;
            }
        }
    }

    @Override
    public List<Order> findBySellerProducts(Long sellerId) throws Exception {
        String sql = "SELECT DISTINCT o.* FROM orders o " +
                     "JOIN order_items oi ON oi.order_id = o.id " +
                     "JOIN products p ON p.id = oi.product_id " +
                     "WHERE p.seller_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) orders.add(mapOrder(rs));
                return orders;
            }
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setBuyerId(rs.getLong("buyer_id"));
        o.setStatus(rs.getString("status"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) o.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()));
        return o;
    }
    private List<OrderItem> findItemsByOrderId(Long orderId) throws Exception {
        String sql = "SELECT oi.*, p.name AS product_name FROM order_items oi " +
                     "JOIN products p ON p.id = oi.product_id WHERE oi.order_id = ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getLong("id"));
                    oi.setOrderId(rs.getLong("order_id"));
                    oi.setProductId(rs.getLong("product_id"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setUnitPrice(rs.getBigDecimal("unit_price"));
                    oi.setProductName(rs.getString("product_name"));
                    items.add(oi);
                }
                return items;
            }
        }
    }
}
