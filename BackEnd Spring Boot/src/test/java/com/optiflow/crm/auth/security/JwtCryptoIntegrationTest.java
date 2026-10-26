package com.optiflow.crm.auth.security;

import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtCryptoIntegrationTest {

    private static final String ISSUER =
            "https://optiflow.example";

    private static final long EXPIRATION_SECONDS = 3600;

    // Clave exclusiva de pruebas, nunca de producción.
    private static final byte[] TEST_KEY =
            "OptiFlow-Test-Secret-Key-2026-Only"
                    .getBytes(StandardCharsets.UTF_8);

    private NimbusJwtEncoder encoder;
    private NimbusJwtDecoder decoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        SecretKey secretKey = new SecretKeySpec(TEST_KEY, "HmacSHA256");

        encoder = NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();

        decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));

        jwtService = new JwtService(encoder);

        ReflectionTestUtils.setField(
                jwtService, "expirationSeconds", EXPIRATION_SECONDS);
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
        user.setRole(UserRole.SELLER);
        user.setActive(true);

        return user;
    }

    private String signToken(
            String subject,
            String issuer,
            Instant issuedAt,
            Instant expiresAt) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", "SELLER")
                .claim("userId", 1L)
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    // =====================================
    // 1. FIRMA Y DECODIFICACIÓN REAL
    // =====================================

    @Test
    void givenValidUser_whenGenerateToken_thenDecodeSuccessfully() {

        // GIVEN
        User user = createUser();

        // WHEN
        String token = jwtService.generateToken(user);
        Jwt decoded = decoder.decode(token);

        // THEN
        assertNotNull(token);

        assertEquals(3, token.split("\\.").length);

        assertAll(
                () -> assertEquals(user.getEmail(), decoded.getSubject()),
                () -> assertEquals(ISSUER, decoded.getClaimAsString("iss")),
                () -> assertEquals("SELLER", decoded.getClaimAsString("role")),
                () -> assertEquals(1L, ((Number) decoded.getClaim("userId")).longValue()));
    }

    // =====================================
    // 2. DURACIÓN DEL TOKEN
    // =====================================

    @Test
    void givenGeneratedToken_whenDecode_thenExpirationIsCorrect() {

        // GIVEN
        User user = createUser();

        // WHEN
        Jwt decoded = decoder.decode(jwtService.generateToken(user));

        // THEN
        assertNotNull(decoded.getIssuedAt());
        assertNotNull(decoded.getExpiresAt());

        assertEquals(EXPIRATION_SECONDS, decoded.getExpiresAt().getEpochSecond()
                        - decoded.getIssuedAt().getEpochSecond());
    }

    // =====================================
    // 3. TOKEN CADUCADO
    // =====================================

    @Test
    void givenExpiredToken_whenDecode_thenReject() {

        // GIVEN
        Instant now = Instant.now();

        String token = signToken("carlos@optiflow.com", ISSUER,
                now.minusSeconds(7200),
                now.minusSeconds(3600));

        // WHEN + THEN
        assertThrows(JwtValidationException.class, () -> decoder.decode(token));
    }

    // =====================================
    // 4. FIRMA MANIPULADA
    // =====================================

    @Test
    void givenTamperedSignature_whenDecode_thenReject() {

        // GIVEN
        String token = jwtService.generateToken(createUser());

        String[] parts = token.split("\\.");

        String signature = parts[2];

        // Alteramos inequívocamente la firma.
        char replacement = signature.charAt(0) == 'A' ? 'B' : 'A';

        String tamperedSignature = replacement + signature.substring(1);

        String tamperedToken =
                parts[0] + "." + parts[1] + "." + tamperedSignature;

        // WHEN + THEN
        assertThrows(JwtException.class, () -> decoder.decode(tamperedToken));
    }

    // =====================================
    // 5. FIRMA CON CLAVE DIFERENTE
    // =====================================

    @Test
    void givenTokenSignedWithAnotherKey_whenDecode_thenReject() {

        // GIVEN
        SecretKey differentKey = new SecretKeySpec("Another-Different-Secret-Key-For-Test"
                        .getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        NimbusJwtEncoder otherEncoder = NimbusJwtEncoder
                .withSecretKey(differentKey)
                .algorithm(MacAlgorithm.HS256).build();

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject("carlos@optiflow.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();

        String token = otherEncoder.encode(
                JwtEncoderParameters.from(claims)).getTokenValue();

        // WHEN + THEN
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    // =====================================
    // 6. ISSUER INCORRECTO
    // =====================================

    @Test
    void givenTokenWithWrongIssuer_whenDecode_thenReject() {

        // GIVEN
        Instant now = Instant.now();

        String token = signToken("carlos@optiflow.com",
                "https://otro-sistema.example",
                now, now.plusSeconds(3600));

        // WHEN + THEN
        assertThrows(JwtValidationException.class, () -> decoder.decode(token));
    }
}
