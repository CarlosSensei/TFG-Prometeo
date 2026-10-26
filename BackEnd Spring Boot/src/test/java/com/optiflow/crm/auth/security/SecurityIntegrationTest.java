package com.optiflow.crm.auth.security;

import com.optiflow.crm.user.model.UserService;
import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Jwt createJwt(String token, String role) {

        Instant now = Instant.now();

        return Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .subject("usuario@optiflow.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("role", role)
                .claim("userId", 1L)
                .build();
    }


    private void mockToken(String token, String role) {

        // Simulamos la decodificación criptográfica del JWT.
        when(jwtDecoder.decode(token)).thenReturn(createJwt(token, role));

        // Simulamos el usuario real almacenado en PostgreSQL.
        User user = new User();

        user.setEmail("usuario@optiflow.com");
        user.setRole(UserRole.valueOf(role));
        user.setActive(true);

        when(userRepository.findByEmailIgnoreCase("usuario@optiflow.com"))
                .thenReturn(Optional.of(user));
    }


    private void assertAuthenticationRequired(int status) {

        assertEquals(401, status,
                "Se esperaba HTTP 401, pero se obtuvo: " + status);
    }

    // =====================================
    // 1. ANÓNIMO - CLIENTES
    // =====================================

    @Test
    void givenAnonymousUser_whenFindClients_thenReject() throws Exception {

        int status = mockMvc.perform(get("/clients"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertAuthenticationRequired(status);
    }

    // =====================================
    // 2. ANÓNIMO - PRESUPUESTOS
    // =====================================

    @Test
    void givenAnonymousUser_whenFindQuotes_thenReject() throws Exception {

        int status = mockMvc.perform(get("/quotes"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertAuthenticationRequired(status);
    }

    // =====================================
    // 3. ANÓNIMO - PEDIDOS
    // =====================================

    @Test
    void givenAnonymousUser_whenFindOrders_thenReject() throws Exception {

        int status = mockMvc.perform(get("/orders"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertAuthenticationRequired(status);
    }

    // =====================================
    // 4. ANÓNIMO - INVENTARIO
    // =====================================

    @Test
    void givenAnonymousUser_whenFindInventory_thenReject() throws Exception {

        int status = mockMvc.perform(get("/inventory"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertAuthenticationRequired(status);
    }

    // =====================================
    // 5. ANÓNIMO - BÚSQUEDA DE PEDIDOS
    // =====================================

    @Test
    void givenAnonymousUser_whenSearchOrders_thenReject() throws Exception {

        String json = """
                {
                    "page": 0,
                    "size": 10
                }
                """;

        int status = mockMvc.perform(post("/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andReturn().getResponse().getStatus();

        assertAuthenticationRequired(status);
    }

    // =====================================
    // 6. ANÓNIMO - CONSULTAR USUARIOS
    // =====================================

    @Test
    void givenAnonymousUser_whenGetUsers_thenReturn401() throws Exception {

        // WHEN + THEN
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    // =====================================
    // 7. ANÓNIMO - CREAR USUARIO
    // =====================================

    @Test
    void givenAnonymousUser_whenCreateUser_thenReturn401() throws Exception {

        // WHEN + THEN
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")).andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    // =====================================
    // 8. SELLER - CONSULTAR USUARIOS
    // =====================================

    @Test
    void givenSellerToken_whenGetUsers_thenReturn200() throws Exception {

        // GIVEN
        mockToken("seller-token", "SELLER");

        when(userService.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // WHEN + THEN
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer seller-token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());

        // VERIFY
        verify(jwtDecoder).decode("seller-token");
        verify(userService).findAll(any(Pageable.class));
    }

    // =====================================
    // 9. SELLER - CREAR USUARIO PROHIBIDO
    // =====================================

    @Test
    void givenSellerToken_whenCreateUser_thenReturn403() throws Exception {

        // GIVEN
        mockToken("seller-token", "SELLER");

        // WHEN + THEN
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer seller-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")).andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================
    // 10. OPTICIAN - CREAR USUARIO PROHIBIDO
    // =====================================

    @Test
    void givenOpticianToken_whenCreateUser_thenReturn403() throws Exception {

        // GIVEN
        mockToken("optician-token", "OPTICIAN");

        // WHEN + THEN
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer optician-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")).andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================
    // 11. ADMIN - AUTORIZACIÓN CORRECTA
    // =====================================

    @Test
    void givenAdminToken_whenCreateInvalidUser_thenReachValidation() throws Exception {

        // GIVEN
        mockToken("admin-token", "ADMIN");

        // WHEN + THEN
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")).andExpect(status().isBadRequest());

        // El ADMIN supera el filtro de seguridad.
        // El DTO inválido se rechaza con HTTP 400.
        verifyNoInteractions(userService);
    }

    // =====================================
    // 12. SELLER - ELIMINAR USUARIO PROHIBIDO
    // =====================================

    @Test
    void givenSellerToken_whenDeleteUser_thenReturn403() throws Exception {

        // GIVEN
        mockToken("seller-token", "SELLER");

        // WHEN + THEN
        mockMvc.perform(delete("/users/10")
                        .header("Authorization", "Bearer seller-token"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================
    // 13. ADMIN - ELIMINAR USUARIO
    // =====================================

    @Test
    void givenAdminToken_whenDeleteUser_thenReturn204() throws Exception {

        // GIVEN
        mockToken("admin-token", "ADMIN");

        // WHEN + THEN
        mockMvc.perform(delete("/users/10")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(userService).delete(10L);
    }

    // =====================================
    // 14. TOKEN INVÁLIDO
    // =====================================

    @Test
    void givenInvalidBearerToken_whenGetUsers_thenReturn401() throws Exception {

        // GIVEN
        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new BadJwtException("Invalid token"));

        // WHEN + THEN
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(jwtDecoder).decode("invalid-token");
        verifyNoInteractions(userService);
    }

    // =====================================
    // 15. LOGIN PÚBLICO CON JSON INVÁLIDO
    // =====================================

    @Test
    void givenAnonymousUser_whenLoginWithInvalidJson_thenReturn400() throws Exception {

        // GIVEN
        String invalidJson = """
                {
                    "email": "invalid-email",
                    "password": ""
                }
                """;

        // WHEN + THEN
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)).andExpect(status().isBadRequest());

        // El filtro de seguridad permite acceder
        // al login sin autenticación.
        verifyNoInteractions(userService);
    }

    // =====================================
    // 16. DEACTIVATED USER SHOULD NOT ACCESS
    // =====================================

    @Test
    void givenDisabledUserWithValidJwt_whenAccessUsers_thenReturn401() throws Exception {

        // GIVEN
        when(jwtDecoder.decode("disabled-user-token"))
                .thenReturn(createJwt("disabled-user-token", "SELLER"));

        User disabledUser = new User();
        disabledUser.setEmail("usuario@optiflow.com");
        disabledUser.setRole(UserRole.SELLER);
        disabledUser.setActive(false);

        when(userRepository.findByEmailIgnoreCase("usuario@optiflow.com"))
                .thenReturn(Optional.of(disabledUser));

        // WHEN + THEN
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer disabled-user-token"))
                .andExpect(status().isUnauthorized());

        // VERIFY
        verify(jwtDecoder).decode("disabled-user-token");
        verify(userRepository).findByEmailIgnoreCase("usuario@optiflow.com");
        verifyNoInteractions(userService);
    }


    // =====================================
    // 17. OLD ADMIN LOSES PRIVILEGE
    // =====================================

    @Test
    void givenDemotedAdminWithOldJwt_whenDeleteUser_thenReturn403() throws Exception {

        // GIVEN
        // El JWT fue emitido cuando el usuario era ADMIN.
        when(jwtDecoder.decode("old-admin-token"))
                .thenReturn(createJwt("old-admin-token", "ADMIN"));

        // Actualmente PostgreSQL indica que es SELLER.
        User currentUser = new User();

        currentUser.setEmail("usuario@optiflow.com");
        currentUser.setRole(UserRole.SELLER);
        currentUser.setActive(true);

        when(userRepository.findByEmailIgnoreCase("usuario@optiflow.com"))
                .thenReturn(Optional.of(currentUser));

        // WHEN + THEN
        mockMvc.perform(delete("/users/10")
                        .header("Authorization", "Bearer old-admin-token"))
                .andExpect(status().isForbidden());

        // VERIFY
        verify(jwtDecoder).decode("old-admin-token");
        verify(userRepository).findByEmailIgnoreCase("usuario@optiflow.com");
        verifyNoInteractions(userService);
    }



}
