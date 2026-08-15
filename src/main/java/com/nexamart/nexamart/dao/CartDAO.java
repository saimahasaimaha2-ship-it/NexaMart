package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.model.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartDAO {
    CartItem addOrUpdate(Long userId, Long productId, int quantity) throws Exception;
    List<CartItem> findByUser(Long userId) throws Exception;
    void removeItem(Long userId, Long productId) throws Exception;
    void clearCart(Long userId) throws Exception;
    Optional<CartItem> findOne(Long userId, Long productId) throws Exception;
}
