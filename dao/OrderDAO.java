package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.model.Order;
import java.sql.Connection;
import java.util.List;

public interface OrderDAO {
    Order createOrderWithItems(Connection conn, Order order) throws Exception;
    List<Order> findByBuyer(Long buyerId) throws Exception;
    List<Order> findBySellerProducts(Long sellerId) throws Exception;
}
