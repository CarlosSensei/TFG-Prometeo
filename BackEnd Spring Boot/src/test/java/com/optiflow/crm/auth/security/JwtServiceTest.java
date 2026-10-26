package com.optiflow.crm.auth.security;

import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private JwtService jwtService;

    private static final long EXPIRATION_SECONDS = 3600L;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        // @Value no se inyecta automáticamente
        // porque no estamos levantando Spring.
        ReflectionTestUtils.setField(jwtService, "expirationSeconds",
                EXPIRATION_SECONDS);
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

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

    private void mockEncodedToken(String tokenValue) {

        Jwt jwt = mock(Jwt.class);

        when(jwt.getTokenValue()).thenReturn(tokenValue);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
    }

    private JwtClaimsSet captureClaims() {

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        return captor.getValue().getClaims();
    }

    // =====================================
    // 1. GENERACIÓN DE TOKEN
    // =====================================

    @Test
    void givenValidUser_whenGenerateToken_thenReturnEncodedToken() {

        // GIVEN
        User user = createUser();

        mockEncodedToken("jwt-token-example");

        // WHEN
        String result = jwtService.generateToken(user);

        // THEN
        assertNotNull(result);
        assertEquals("jwt-token-example", result);

        verify(jwtEncoder, times(1))
                .encode(any(JwtEncoderParameters.class));
    }

    // =====================================
    // 2. ISSUER
    // =====================================

    @Test
    void givenUser_whenGenerateToken_thenSetCorrectIssuer() {

        // GIVEN
        User user = createUser();
        mockEncodedToken("jwt-token");

        // WHEN
        jwtService.generateToken(user);

        // THEN
        JwtClaimsSet claims = captureClaims();

        assertEquals("https://optiflow.example", claims.getIssuer().toString());
    }

    // =====================================
    // 3. SUBJECT
    // =====================================

    @Test
    void givenUser_whenGenerateToken_thenSetEmailAsSubject() {

        // GIVEN
        User user = createUser();

        mockEncodedToken("jwt-token");

        // WHEN
        jwtService.generateToken(user);

        // THEN
        JwtClaimsSet claims = captureClaims();

        assertEquals("carlos@optiflow.com", claims.getSubject());
    }

    // =====================================
    // 4. USER ID
    // =====================================

    @Test
    void givenUser_whenGenerateToken_thenIncludeUserId() {

        // GIVEN
        User user = createUser();

        mockEncodedToken("jwt-token");

        // WHEN
        jwtService.generateToken(user);

        // THEN
        JwtClaimsSet claims = captureClaims();

        assertEquals(1L, claims.getClaims().get("userId"));
    }

    // =====================================
    // 5. ROLE
    // =====================================

    @Test
    void givenSeller_whenGenerateToken_thenIncludeSellerRole() {

        // GIVEN
        User user = createUser();

        mockEncodedToken("jwt-token");

        // WHEN
        jwtService.generateToken(user);

        // THEN
        JwtClaimsSet claims = captureClaims();

        assertEquals("SELLER", claims.getClaims().get("role"));
    }

    // =====================================
    // 6. DIFFERENT USER ROLE
    // =====================================

    @Test
    void givenOptician_whenGenerateToken_thenIncludeOpticianRole() {

        // GIVEN
        User user = createUser();
        user.setRole(UserRole.OPTICIAN);

        mockEncodedToken("jwt-optician");

        // WHEN
        String result = jwtService.generateToken(user);

        // THEN
        JwtClaimsSet claims = captureClaims();

        assertEquals("OPTICIAN", claims.getClaims().get("role"));
        assertEquals("jwt-optician", result);
    }

    // =====================================
    // 7. ISSUE AND EXPIRATION DATES
    // =====================================

    @Test
    void givenExpirationConfig_whenGenerateToken_thenSetCorrectDates() {

        // GIVEN
        User user = createUser();

        mockEncodedToken("jwt-token");

        Instant before = Instant.now();

        // WHEN
        jwtService.generateToken(user);

        Instant after = Instant.now();

        // THEN
        JwtClaimsSet claims = captureClaims();

        Instant issuedAt = claims.getIssuedAt();
        Instant expiresAt = claims.getExpiresAt();

        assertNotNull(issuedAt);
        assertNotNull(expiresAt);

        assertFalse(issuedAt.isBefore(before));
        assertFalse(issuedAt.isAfter(after));

        assertEquals(
                Duration.ofSeconds(EXPIRATION_SECONDS),
                Duration.between(issuedAt, expiresAt)
        );
    }

    // =====================================
    // 8. ENCODER FAILURE
    // =====================================

    @Test
    void givenEncoderFailure_whenGenerateToken_thenPropagateException() {

        // GIVEN
        User user = createUser();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenThrow(
                        new JwtEncodingException("No se pudo firmar el token"));

        // WHEN + THEN
        JwtEncodingException exception = assertThrows(JwtEncodingException.class,
                () -> jwtService.generateToken(user));

        assertEquals("No se pudo firmar el token", exception.getMessage());

        verify(jwtEncoder, times(1))
                .encode(any(JwtEncoderParameters.class));
    }
}
