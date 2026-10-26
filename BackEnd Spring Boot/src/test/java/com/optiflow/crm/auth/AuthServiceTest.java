
package com.optiflow.crm.auth;

import com.optiflow.crm.auth.dto.LoginRequest;
import com.optiflow.crm.auth.dto.LoginResponse;
import com.optiflow.crm.auth.security.JwtService;

import com.optiflow.crm.shared.exception.UserNotFoundException;
import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private LoginRequest createLoginRequest() {

        LoginRequest request = new LoginRequest();

        request.setEmail("carlos@optiflow.com");
        request.setPassword("Password123!");

        return request;
    }

    private User createUser() {

        User user = new User();

        user.setId(1L);
        user.setFirstName("Carlos");
        user.setLastName("García");
        user.setEmail("carlos@optiflow.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.SELLER);
        user.setActive(true);

        return user;
    }

    // =====================================
    // 1. LOGIN CORRECTO
    // =====================================

    @Test
    void givenValidCredentials_whenLogin_thenReturnTokenAndUser() {

        // GIVEN
        LoginRequest request = createLoginRequest();
        User user = createUser();

        when(userRepository.findByEmailIgnoreCase("carlos@optiflow.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token-example");

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertNotNull(response);

        assertAll(
                () -> assertEquals("jwt-token-example", response.token()),
                () -> assertEquals("Bearer", response.tokenType()),
                () -> assertEquals(1L, response.userId()),
                () -> assertEquals("carlos@optiflow.com", response.email()),
                () -> assertEquals("Carlos", response.firstName()),
                () -> assertEquals("García", response.lastName()),
                () -> assertEquals(UserRole.SELLER, response.role()));

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }

    // =====================================
    // 2. NORMALIZACIÓN DEL EMAIL
    // =====================================

    @Test
    void givenUppercaseEmailWithSpaces_whenLogin_thenNormalizeBeforeAuthentication() {

        // GIVEN
        LoginRequest request = createLoginRequest();

        request.setEmail("  CARLOS@OPTIFLOW.COM  ");

        User user = createUser();

        when(userRepository.findByEmailIgnoreCase("carlos@optiflow.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        // WHEN
        LoginResponse result = authService.login(request);

        // THEN
        assertNotNull(result);

        ArgumentCaptor<Authentication> captor =
                ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager).authenticate(captor.capture());

        Authentication authentication = captor.getValue();

        assertEquals("carlos@optiflow.com", authentication.getPrincipal());
        assertEquals("Password123!", authentication.getCredentials());

        verify(userRepository).findByEmailIgnoreCase("carlos@optiflow.com");
    }

    // =====================================
    // 3. CONTRASEÑA INCORRECTA
    // =====================================

    @Test
    void givenInvalidPassword_whenLogin_thenThrowBadCredentials() {

        // GIVEN
        LoginRequest request = createLoginRequest();
        request.setPassword("WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // WHEN + THEN
        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        // La autenticación falla antes de buscar
        // al usuario o generar un JWT.
        verifyNoInteractions(userRepository, jwtService);
    }

    // =====================================
    // 4. USUARIO NO ENCONTRADO
    // =====================================

    @Test
    void givenAuthenticatedEmailNotInRepository_whenLogin_thenThrowNotFound() {

        // GIVEN
        LoginRequest request = createLoginRequest();

        when(userRepository.findByEmailIgnoreCase("carlos@optiflow.com"))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(UserNotFoundException.class, () -> authService.login(request));

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class));

        verifyNoInteractions(jwtService);
    }

    // =====================================
    // 5. NO GENERAR TOKEN SI FALLA EL LOGIN
    // =====================================

    @Test
    void givenAuthenticationFailure_whenLogin_thenNeverGenerateJwt() {

        // GIVEN
        LoginRequest request = createLoginRequest();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // WHEN + THEN
        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(any(User.class));
        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
    }

    // =====================================
    // 6. ROL DEL USUARIO
    // =====================================

    @Test
    void givenOpticianUser_whenLogin_thenReturnCorrectRole() {

        // GIVEN
        LoginRequest request = createLoginRequest();

        User user = createUser();
        user.setRole(UserRole.OPTICIAN);

        when(userRepository.findByEmailIgnoreCase("carlos@optiflow.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-optician");

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertEquals(UserRole.OPTICIAN, response.role());
        assertEquals("jwt-optician", response.token());
    }

    // =====================================
    // 7. CONTRASEÑA ORIGINAL EN AUTENTICACIÓN
    // =====================================

    @Test
    void givenValidLogin_whenAuthenticate_thenUseRawPassword() {

        // GIVEN
        LoginRequest request = createLoginRequest();
        User user = createUser();

        when(userRepository.findByEmailIgnoreCase("carlos@optiflow.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        // WHEN
        authService.login(request);

        // THEN
        ArgumentCaptor<Authentication> captor = ArgumentCaptor
                .forClass(Authentication.class);

        verify(authenticationManager).authenticate(captor.capture());

        Authentication authentication = captor.getValue();

        // AuthenticationManager recibe la contraseña
        // original y verifica su hash internamente.
        assertEquals("Password123!", authentication.getCredentials());
        assertNotEquals(user.getPassword(), authentication.getCredentials());
    }
}
