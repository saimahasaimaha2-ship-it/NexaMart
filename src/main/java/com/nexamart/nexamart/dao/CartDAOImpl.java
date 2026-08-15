package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.listener.DataSourceListener;
import com.nexamart.nexamart.model.CartItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartDAOImpl implements CartDAO {

    @Override
    public CartItem addOrUpdate(Long userId, Long productId, int quantity) throws Exception {
        Optional<CartItem> existing = findOne(userId, productId);
        try (Connection conn = DataSourceListener.getDataSource().getConnection()) {
            if (existing.isPresent()) {
                String sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, quantity);
                    ps.setLong(2, userId);
                    ps.setLong(3, productId);
                    ps.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, userId);
                    ps.setLong(2, productId);
                    ps.setInt(3, quantity);
                    ps.executeUpdate();
                }
            }
        }
        return findOne(userId, productId).orElseThrow();
    }

    @Override
    public List<CartItem> findByUser(Long userId) throws Exception {
        String sql = "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, p.name AS product_name, p.price " +
                     "FROM cart_items ci JOIN products p ON ci.product_id = p.id WHERE ci.user_id = ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<CartItem> items = new ArrayList<>();
                while (rs.next()) items.add(map(rs));
                return items;
            }
        }
    }

    @Override
    public void removeItem(Long userId, Long productId) throws Exception {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }
    }

    @Override
    public void clearCart(Long userId) throws Exception {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<CartItem> findOne(Long userId, Long productId) throws Exception {
        String sql = "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, p.name AS product_name, p.price " +
                     "FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                     "WHERE ci.user_id = ? AND ci.product_id = ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private CartItem map(ResultSet rs) throws SQLException {
        CartItem c = new CartItem();
        c.setId(rs.getLong("id"));
        c.setUserId(rs.getLong("user_id"));
        c.setProductId(rs.getLong("product_id"));
        c.setQuantity(rs.getInt("quantity"));
        c.setProductName(rs.getString("product_name"));
        c.setUnitPrice(rs.getBigDecimal("price"));
        return c;
    }
}
