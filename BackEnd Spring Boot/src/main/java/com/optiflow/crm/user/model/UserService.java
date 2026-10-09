package com.optiflow.crm.user.model;

import com.optiflow.crm.user.dto.CreateUserDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    User findById(Long id);

    User findByEmail(String email);

    User create(CreateUserDto dto);

    void delete(Long id);
}