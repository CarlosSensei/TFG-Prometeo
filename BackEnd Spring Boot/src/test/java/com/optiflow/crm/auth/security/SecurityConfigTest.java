package com.optiflow.crm.auth.security;

import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private JwtAuthenticationConverter converter;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

        securityConfig = new SecurityConfig();

        userRepository = mock(UserRepository.class);

        JwtUserAuthoritiesConverter authoritiesConverter =
                new JwtUserAuthoritiesConverter(userRepository);

        converter = securityConfig.jwtAuthenticationConverter(authoritiesConverter);
    }


    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Jwt createJwt(String role) {

        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("usuario@optiflow.com");

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.build();
    }


    private Set<String> extractAuthorities(Jwt jwt) {

        // GIVEN
        String email = jwt.getSubject();
        String roleClaim = jwt.getClaimAsString("role");

        User user = new User();

        user.setEmail(email);
        user.setActive(true);

        if (roleClaim != null) {
            user.setRole(UserRole.valueOf(roleClaim));
        } else {
            user.setRole(null);
        }

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(user));

        // WHEN
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertNotNull(authentication);

        // THEN
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }


    private CorsConfiguration getCorsConfiguration() {

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/clients");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);

        return configuration;
    }

    private Set<String> extractRoles(Set<String> authorities) {

        return authorities.stream()
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }

    // =====================================
    // 1. ROLE_ADMIN
    // =====================================

    @Test
    void givenAdminRole_whenConvertJwt_thenGrantRoleAdmin() {

        // GIVEN
        Jwt jwt = createJwt("ADMIN");

        // WHEN
        Set<String> authorities = extractAuthorities(jwt);

        // THEN
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertFalse(authorities.contains("ROLE_SELLER"));
        assertFalse(authorities.contains("ROLE_OPTICIAN"));
        assertEquals(Set.of("ROLE_ADMIN"), extractRoles(authorities));
    }

    // =====================================
    // 2. ROLE_SELLER
    // =====================================

    @Test
    void givenSellerRole_whenConvertJwt_thenGrantRoleSeller() {

        // GIVEN
        Jwt jwt = createJwt("SELLER");

        // WHEN
        Set<String> authorities = extractAuthorities(jwt);

        // THEN
        assertTrue(authorities.contains("ROLE_SELLER"));
        assertFalse(authorities.contains("ROLE_ADMIN"));
    }

    // =====================================
    // 3. ROLE_OPTICIAN
    // =====================================

    @Test
    void givenOpticianRole_whenConvertJwt_thenGrantRoleOptician() {

        // GIVEN
        Jwt jwt = createJwt("OPTICIAN");

        // WHEN
        Set<String> authorities = extractAuthorities(jwt);

        // THEN
        assertTrue(authorities.contains("ROLE_OPTICIAN"));
        assertFalse(authorities.contains("ROLE_ADMIN"));
    }

    // =====================================
    // 4. USER SIN ROLE
    // =====================================

    @Test
    void givenUserWithoutRole_whenConvertJwt_thenRejectAuthentication() {

        // GIVEN
        Jwt jwt = createJwt(null);

        User user = new User();
        user.setEmail(jwt.getSubject());
        user.setActive(true);
        user.setRole(null);

        when(userRepository.findByEmailIgnoreCase(jwt.getSubject())).thenReturn(Optional.of(user));

        // WHEN + THEN
        assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(jwt));

        verify(userRepository).findByEmailIgnoreCase(jwt.getSubject());
    }



    // =====================================
    // 5. ORIGEN ANGULAR PERMITIDO
    // =====================================

    @Test
    void givenAngularOrigin_whenCheckCors_thenAllowOrigin() {

        // GIVEN
        CorsConfiguration configuration = getCorsConfiguration();

        // WHEN
        String allowedOrigin = configuration.checkOrigin("http://localhost:4200");

        // THEN
        assertEquals("http://localhost:4200", allowedOrigin);
    }

    // =====================================
    // 6. ORIGEN NO AUTORIZADO
    // =====================================

    @Test
    void givenUnknownOrigin_whenCheckCors_thenRejectOrigin() {

        // GIVEN
        CorsConfiguration configuration = getCorsConfiguration();

        // WHEN
        String allowedOrigin = configuration.checkOrigin("https://malicious.example");

        // THEN
        assertNull(allowedOrigin);
    }

    // =====================================
    // 7. MÉTODOS HTTP PERMITIDOS
    // =====================================

    @Test
    void givenCorsConfiguration_whenCheckMethods_thenAllowRequiredMethods() {

        // GIVEN
        CorsConfiguration configuration = getCorsConfiguration();

        // WHEN + THEN
        assertNotNull(configuration.getAllowedMethods());
        assertTrue(configuration.getAllowedMethods().containsAll(
                        Set.of(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS"
                        )));

        assertTrue(configuration.getAllowedHeaders().contains("*"));
        assertTrue(configuration.getExposedHeaders().contains("Authorization"));
    }
}
