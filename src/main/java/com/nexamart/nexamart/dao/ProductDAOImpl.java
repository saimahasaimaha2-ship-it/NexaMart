package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.listener.DataSourceListener;
import com.nexamart.nexamart.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public Product create(Product p) throws Exception {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getSellerId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStockQty());
            ps.setString(6, p.getCategory());
            ps.setString(7, p.getImageUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getLong(1));
            }
            return p;
        }
    }

    @Override
    public Optional<Product> findById(Long id) throws Exception {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Product> search(String keyword, String category) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?)");
            String like = "%" + keyword.toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> results = new ArrayList<>();
                while (rs.next()) results.add(map(rs));
                return results;
            }
        }
    }

    @Override
    public void decrementStock(Long productId, int qty) throws Exception {
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setLong(2, productId);
            ps.setInt(3, qty);
            int updated = ps.executeUpdate();
            if (updated == 0) throw new IllegalStateException("Insufficient stock for product " + productId);
        }
    }
    @Override
    public void update(Product p) throws Exception {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, category = ?, image_url = ? " +
                     "WHERE id = ? AND seller_id = ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getStockQty());
            ps.setString(5, p.getCategory());
            ps.setString(6, p.getImageUrl());
            ps.setLong(7, p.getId());
            ps.setLong(8, p.getSellerId());
            int updated = ps.executeUpdate();
            if (updated == 0) throw new IllegalStateException("Product not found or not owned by seller");
        }
    }

    @Override
    public void delete(Long id, Long sellerId) throws Exception {
        String sql = "DELETE FROM products WHERE id = ? AND seller_id = ?";
        try (Connection conn = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, sellerId);
            int deleted = ps.executeUpdate();
            if (deleted == 0) throw new IllegalStateException("Product not found or not owned by seller");
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()));
        return p;
    }
}