package com.optiflow.crm.auth;

import com.optiflow.crm.shared.exception.ResourceNotFoundException;
import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
// Service para recuperar el user actual
@Service
public class CurrentUserService {

    // Inyeccion del repository
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Metodo para recuperar el user
    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                        new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}