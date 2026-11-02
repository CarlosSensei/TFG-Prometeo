package com.optiflow.crm.auth;

import com.optiflow.crm.auth.dto.LoginRequest;
import com.optiflow.crm.auth.dto.LoginResponse;
import com.optiflow.crm.auth.security.JwtService;

import com.optiflow.crm.shared.exception.UserNotFoundException;
import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

// Service de la autenticacion
@Service
public class AuthService {

    // Inyeccion de repository, service y authmanager de spring
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
            UserRepository userRepository, JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // Logica del login y respuesta del user
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );

        } catch (AuthenticationException ex) {

            System.err.println(
                    "Error de autenticación: " +
                            ex.getClass().getSimpleName()
            );

            throw ex;
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new UserNotFoundException(email));

        String token = jwtService.generateToken(user);

        return new LoginResponse(token, "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole());
    }
}