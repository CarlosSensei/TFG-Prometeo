package com.optiflow.crm.auth.security;

import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUserAuthoritiesConverterTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtUserAuthoritiesConverter converter;

    private static final String EMAIL = "usuario@optiflow.com";

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Jwt createJwt(String email, String role) {

        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "HS256");

        if (email != null) {
            builder.subject(email);
        }

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.build();
    }

    private User createUser(UserRole role, boolean active) {

        User user = new User();

        user.setEmail(EMAIL);
        user.setRole(role);
        user.setActive(active);

        return user;
    }

    private Set<String> extractAuthorities(Jwt jwt) {

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    // =====================================
    // 1. ADMIN ACTIVO
    // =====================================

    @Test
    void givenActiveAdmin_whenConvert_thenGrantAdminRole() {

        // GIVEN
        Jwt jwt = createJwt(EMAIL, "ADMIN");

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(createUser(UserRole.ADMIN, true)));

        // WHEN
        Set<String> authorities = extractAuthorities(jwt);

        // THEN
        assertEquals(Set.of("ROLE_ADMIN"), authorities);

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
    }

    // =====================================
    // 2. SELLER ACTIVO
    // =====================================

    @Test
    void givenActiveSeller_whenConvert_thenGrantSellerRole() {

        // GIVEN
        Jwt jwt = createJwt(EMAIL, "SELLER");

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(createUser(UserRole.SELLER, true)));

        // WHEN
        Set<String> authorities = extractAuthorities(jwt);

        // THEN
        assertEquals(Set.of("ROLE_SELLER"), authorities);
        assertFalse(authorities.contains("ROLE_ADMIN"));
    }

    // =====================================
    // 3. OPTICIAN ACTIVO
    // =====================================

    @Test
    void givenActiveOptician_whenConvert_thenGrantOpticianRole() {

        // GIVEN
        Jwt jwt = createJwt(EMAIL, "OPTICIAN");

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(createUser(UserRole.OPTICIAN, true)));

        // WHEN
        Set<String> authorities = extractAuthorities(jwt);

        // THEN
        assertEquals(Set.of("ROLE_OPTICIAN"), authorities);
    }

    // =====================================
    // 4. USUARIO INEXISTENTE
    // =====================================

    @Test
    void givenUnknownUser_whenConvert_thenRejectAuthentication() {

        // GIVEN
        Jwt jwt = createJwt(EMAIL, "SELLER");

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        // WHEN + THEN
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class, () -> converter.convert(jwt));

        assertEquals("invalid_token", exception.getError().getErrorCode());
    }

    // =====================================
    // 5. USUARIO DESACTIVADO
    // =====================================

    @Test
    void givenInactiveUser_whenConvert_thenRejectAuthentication() {

        // GIVEN
        Jwt jwt = createJwt(EMAIL, "ADMIN");

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(createUser(UserRole.ADMIN, false)));

        // WHEN + THEN
        assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(jwt));

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
    }

    // =====================================
    // 6. USUARIO SIN ROL
    // =====================================

    @Test
    void givenUserWithoutRole_whenConvert_thenRejectAuthentication() {

        // GIVEN
        Jwt jwt = createJwt(EMAIL, "ADMIN");

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(createUser(null, true)));

        // WHEN + THEN
        assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(jwt));
    }

    // =====================================
    // 7. JWT SIN SUBJECT
    // =====================================

    @Test
    void givenJwtWithoutSubject_whenConvert_thenRejectAuthentication() {

        // GIVEN
        Jwt jwt = createJwt(null, "ADMIN");

        // WHEN + THEN
        assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(jwt));

        // No debe consultar BD sin identificar al usuario.
        verifyNoInteractions(userRepository);
    }

    // =====================================
    // 8. ADMIN DEGRADADO A SELLER
    // =====================================

    @Test
    void givenOldAdminJwtButCurrentSeller_whenConvert_thenUseCurrentRole() {

        // GIVEN
        Jwt oldJwt = createJwt(EMAIL, "ADMIN");

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(createUser(UserRole.SELLER, true)));

        // WHEN
        Set<String> authorities = extractAuthorities(oldJwt);

        // THEN
        assertEquals(Set.of("ROLE_SELLER"), authorities);
        assertFalse(authorities.contains("ROLE_ADMIN"));
    }

    // =====================================
    // 9. JWT SIN CLAIM ROLE
    // =====================================

    @Test
    void givenJwtWithoutRoleClaim_whenConvert_thenUseDatabaseRole() {

        // GIVEN
        Jwt jwt = createJwt(EMAIL, null);

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(createUser(UserRole.OPTICIAN, true)));

        // WHEN
        Set<String> authorities = extractAuthorities(jwt);

        // THEN
        assertEquals(Set.of("ROLE_OPTICIAN"), authorities);
    }
}
