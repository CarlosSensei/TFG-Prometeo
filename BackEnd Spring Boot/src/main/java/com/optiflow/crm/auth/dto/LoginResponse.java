package com.optiflow.crm.auth.dto;

import com.optiflow.crm.user.model.UserRole;

// Record del Dto de la respuesta del login
public record LoginResponse(
        String token,
        String tokenType,
        Long userId,
        String email,
        String firstName,
        String lastName,
        UserRole role
) {
}
