package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {
    Product create(Product product) throws Exception;
    Optional<Product> findById(Long id) throws Exception;
    List<Product> search(String keyword, String category) throws Exception;
    void decrementStock(Long productId, int qty) throws Exception;
}
