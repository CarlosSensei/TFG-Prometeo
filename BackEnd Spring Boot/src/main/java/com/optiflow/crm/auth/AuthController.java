package com.optiflow.crm.auth;

import com.optiflow.crm.auth.dto.LoginRequest;
import com.optiflow.crm.auth.dto.LoginResponse;

import com.optiflow.crm.user.dto.UserDto;
import com.optiflow.crm.user.mapper.UserMapper;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller de la autenticacion
@RestController
@RequestMapping("/auth")
public class AuthController {

    // Inyeccion del service y mapper
    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService,
                          CurrentUserService currentUserService,
                          UserMapper userMapper) {

        this.authService = authService;
        this.currentUserService = currentUserService;
        this.userMapper = userMapper;
    }

    // Metodo de login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // Metodo me para enviar al front el user y su rol
    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {

        User user = currentUserService.getCurrentUser();

        return ResponseEntity.ok(userMapper.toDto(user));
    }
}