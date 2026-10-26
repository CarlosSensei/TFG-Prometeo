package com.optiflow.crm.auth;

import com.optiflow.crm.shared.exception.ResourceNotFoundException;
import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private User createUser() {

        User user = new User();

        user.setFirstName("Carlos");
        user.setLastName("García");
        user.setEmail("carlos@optiflow.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.SELLER);
        user.setActive(true);

        return user;
    }

    private void authenticateAs(String email) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // =====================================
    // 1. USUARIO AUTENTICADO EXISTENTE
    // =====================================

    @Test
    void givenAuthenticatedUser_whenGetCurrentUser_thenReturnUser() {

        // GIVEN
        authenticateAs("carlos@optiflow.com");

        User expected = createUser();

        when(userRepository.findByEmailIgnoreCase("carlos@optiflow.com"))
                .thenReturn(Optional.of(expected));

        // WHEN
        User result = currentUserService.getCurrentUser();

        // THEN
        assertSame(expected, result);

        assertAll(
                () -> assertEquals("carlos@optiflow.com", result.getEmail()),
                () -> assertEquals(UserRole.SELLER, result.getRole()),
                () -> assertTrue(result.getActive()));

        verify(userRepository).findByEmailIgnoreCase("carlos@optiflow.com");
        verifyNoMoreInteractions(userRepository);
    }

    // =====================================
    // 2. EMAIL EN MAYÚSCULAS
    // =====================================

    @Test
    void givenUppercaseAuthenticatedEmail_whenGetCurrentUser_thenFindIgnoringCase() {

        // GIVEN
        authenticateAs("CARLOS@OPTIFLOW.COM");

        User expected = createUser();

        when(userRepository.findByEmailIgnoreCase("CARLOS@OPTIFLOW.COM"))
                .thenReturn(Optional.of(expected));

        // WHEN
        User result = currentUserService.getCurrentUser();

        // THEN
        assertSame(expected, result);
        assertEquals("carlos@optiflow.com", result.getEmail());

        verify(userRepository).findByEmailIgnoreCase("CARLOS@OPTIFLOW.COM");
    }

    // =====================================
    // 3. USUARIO NO ENCONTRADO
    // =====================================

    @Test
    void givenAuthenticatedUserNotInDatabase_whenGetCurrentUser_thenThrowNotFound() {

        // GIVEN
        authenticateAs("deleted@optiflow.com");

        when(userRepository.findByEmailIgnoreCase("deleted@optiflow.com"))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> currentUserService.getCurrentUser());

        assertEquals("Usuario autenticado no encontrado", exception.getMessage());

        verify(userRepository).findByEmailIgnoreCase("deleted@optiflow.com");
    }

    // =====================================
    // 4. RECUPERAR EL ROL REAL DEL USUARIO
    // =====================================

    @Test
    void givenAuthenticatedOptician_whenGetCurrentUser_thenReturnOpticianRole() {

        // GIVEN
        authenticateAs("carlos@optiflow.com");

        User expected = createUser();
        expected.setRole(UserRole.OPTICIAN);

        when(userRepository.findByEmailIgnoreCase("carlos@optiflow.com"))
                .thenReturn(Optional.of(expected));

        // WHEN
        User result = currentUserService.getCurrentUser();

        // THEN
        assertSame(expected, result);
        assertEquals(UserRole.OPTICIAN, result.getRole());

        verify(userRepository).findByEmailIgnoreCase("carlos@optiflow.com");
    }

    // =====================================
    // 5. USUARIO DESACTIVADO
    // =====================================


    @Test
    void givenInactiveUser_whenGetCurrentUser_thenRejectAccess() {

        // GIVEN
        authenticateAs("carlos@optiflow.com");

        User inactive = createUser();
        inactive.setActive(false);

        when(userRepository.findByEmailIgnoreCase("carlos@optiflow.com"))
                .thenReturn(Optional.of(inactive));

        // WHEN + THEN
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> currentUserService.getCurrentUser());

        verify(userRepository).findByEmailIgnoreCase("carlos@optiflow.com");
    }

    @Test
    void givenNoAuthentication_whenGetCurrentUser_thenRejectAccess() {

        // GIVEN
        SecurityContextHolder.clearContext();

        // WHEN + THEN
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> currentUserService.getCurrentUser());

        verifyNoInteractions(userRepository);
    }

}
