package com.optiflow.crm.auth;

import com.optiflow.crm.shared.exception.ResourceNotFoundException;
import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                        new ResourceNotFoundException("Usuario autenticado no encontrado"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AuthenticationCredentialsNotFoundException("Usuario desactivado");
        }

        return user;
    }
}
