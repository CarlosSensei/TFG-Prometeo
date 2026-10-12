package com.optiflow.crm.user.model;

import com.optiflow.crm.user.dto.CreateUserDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Interface del service de user
public interface UserService {

    // Buscar todos los users paginado
    Page<User> findAll(Pageable pageable);

    // Buscar por id
    User findById(Long id);

    // Buscar por email
    User findByEmail(String email);

    // Crear un usuario
    User create(CreateUserDto dto);

    // Borrar un usuario
    void delete(Long id);
}