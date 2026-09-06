package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.CartDAO;
import com.nexamart.nexamart.dao.OrderDAO;
import com.nexamart.nexamart.dao.ProductDAO;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private OrderDAO orderDAO;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(cartDAO, productDAO, orderDAO);
    }

    @Test
    void checkout_throwsValidationError_whenCartIsEmpty() throws Exception {
        when(cartDAO.findByUser(1L)).thenReturn(Collections.emptyList());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> orderService.checkout(1L));
        assertEquals("VALIDATION_ERROR", ex.getCode());
        verifyNoInteractions(orderDAO, productDAO);
    }

    @Test
    void buyerHistory_returnsOrders_whenDaoSucceeds() throws Exception {
        Order o = new Order();
        o.setBuyerId(1L);
        when(orderDAO.findByBuyer(1L)).thenReturn(List.of(o));

        List<Order> result = orderService.buyerHistory(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBuyerId());
    }

    @Test
    void buyerHistory_throwsInternalError_whenDaoFails() throws Exception {
        when(orderDAO.findByBuyer(1L)).thenThrow(new RuntimeException("db down"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> orderService.buyerHistory(1L));
        assertEquals("INTERNAL_ERROR", ex.getCode());
    }

    @Test
    void sellerIncomingOrders_returnsOrders_whenDaoSucceeds() throws Exception {
        Order o = new Order();
        o.setBuyerId(2L);
        when(orderDAO.findBySellerProducts(5L)).thenReturn(List.of(o));

        List<Order> result = orderService.sellerIncomingOrders(5L);

        assertEquals(1, result.size());
        verify(orderDAO, times(1)).findBySellerProducts(5L);
    }
}