package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.model.User;
import java.util.Optional;

public interface UserDAO {
    User create(User user) throws Exception;
    Optional<User> findByEmail(String email) throws Exception;
    Optional<User> findById(Long id) throws Exception;
}
