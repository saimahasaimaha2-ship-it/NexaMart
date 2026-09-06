package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.CartDAO;
import com.nexamart.nexamart.dao.ProductDAO;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.CartItem;
import com.nexamart.nexamart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartDAO, productDAO);
    }

    @Test
    void addItem_throwsValidationError_whenQuantityIsZeroOrNegative() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> cartService.addItem(1L, 10L, 0));
        assertEquals("VALIDATION_ERROR", ex.getCode());
        verifyNoInteractions(productDAO, cartDAO);
    }

    @Test
    void addItem_throwsNotFound_whenProductDoesNotExist() throws Exception {
        when(productDAO.findById(10L)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> cartService.addItem(1L, 10L, 2));
        assertEquals("NOT_FOUND", ex.getCode());
        verifyNoInteractions(cartDAO);
    }

    @Test
    void addItem_throwsValidationError_whenStockIsInsufficient() throws Exception {
        Product p = new Product();
        p.setId(10L);
        p.setStockQty(1);
        when(productDAO.findById(10L)).thenReturn(Optional.of(p));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> cartService.addItem(1L, 10L, 5));
        assertEquals("VALIDATION_ERROR", ex.getCode());
        verifyNoInteractions(cartDAO);
    }

    @Test
    void addItem_succeeds_whenStockIsSufficient() throws Exception {
        Product p = new Product();
        p.setId(10L);
        p.setStockQty(5);
        when(productDAO.findById(10L)).thenReturn(Optional.of(p));

        CartItem expected = new CartItem();
        expected.setUserId(1L);
        expected.setProductId(10L);
        expected.setQuantity(2);
        when(cartDAO.addOrUpdate(1L, 10L, 2)).thenReturn(expected);

        CartItem result = cartService.addItem(1L, 10L, 2);

        assertEquals(expected, result);
        verify(cartDAO, times(1)).addOrUpdate(1L, 10L, 2);
    }

    @Test
    void cartTotal_sumsUnitPriceTimesQuantity_acrossItems() throws Exception {
        CartItem item1 = new CartItem();
        item1.setUnitPrice(new BigDecimal("10.00"));
        item1.setQuantity(2);

        CartItem item2 = new CartItem();
        item2.setUnitPrice(new BigDecimal("5.50"));
        item2.setQuantity(3);

        when(cartDAO.findByUser(1L)).thenReturn(List.of(item1, item2));

        BigDecimal total = cartService.cartTotal(1L);

        assertEquals(new BigDecimal("36.50"), total);
    }

    @Test
    void viewCart_throwsInternalError_whenDaoFails() throws Exception {
        when(cartDAO.findByUser(1L)).thenThrow(new RuntimeException("db down"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> cartService.viewCart(1L));
        assertEquals("INTERNAL_ERROR", ex.getCode());
    }
}