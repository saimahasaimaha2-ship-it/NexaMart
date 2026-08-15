package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.CartDAO;
import com.nexamart.nexamart.dao.CartDAOImpl;
import com.nexamart.nexamart.dao.ProductDAO;
import com.nexamart.nexamart.dao.ProductDAOImpl;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.CartItem;
import com.nexamart.nexamart.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class CartService {
    private final CartDAO cartDAO = new CartDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();

    public CartItem addItem(Long userId, Long productId, int quantity) throws ServiceException {
        if (quantity <= 0) throw new ServiceException("VALIDATION_ERROR", "Quantity must be positive");
        try {
            Product p = productDAO.findById(productId)
                    .orElseThrow(() -> new ServiceException("NOT_FOUND", "Product not found"));
            if (p.getStockQty() < quantity) {
                throw new ServiceException("VALIDATION_ERROR", "Insufficient stock");
            }
            return cartDAO.addOrUpdate(userId, productId, quantity);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not add item to cart");
        }
    }

    public List<CartItem> viewCart(Long userId) throws ServiceException {
        try {
            return cartDAO.findByUser(userId);
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not load cart");
        }
    }

    public void removeItem(Long userId, Long productId) throws ServiceException {
        try {
            cartDAO.removeItem(userId, productId);
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not remove item");
        }
    }

    public BigDecimal cartTotal(Long userId) throws ServiceException {
        return viewCart(userId).stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
