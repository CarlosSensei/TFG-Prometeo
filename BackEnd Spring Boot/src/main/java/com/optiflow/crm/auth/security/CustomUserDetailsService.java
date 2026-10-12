package com.optiflow.crm.auth.security;

import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

// Service con logicas de usuarios
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Inyeccion de repository
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Cargar user por mail
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.getActive())
                .build();
    }
}