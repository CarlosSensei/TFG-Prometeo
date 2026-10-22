package com.optiflow.crm.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // =====================================
    // MÉTODO AUXILIAR
    // =====================================

    private void assertAuthenticationRequired(int status) {

        assertTrue(status == 401 || status == 403,
                "Se esperaba HTTP 401 o 403, pero se obtuvo: " + status);
    }

    // =====================================
    // 1. CLIENTES
    // =====================================

    @Test
    void givenAnonymousUser_whenFindClients_thenReject() throws Exception {

        // WHEN
        int status = mockMvc.perform(get("/clients"))
                .andReturn()
                .getResponse()
                .getStatus();

        // THEN
        assertAuthenticationRequired(status);
    }

    // =====================================
    // 2. PRESUPUESTOS
    // =====================================

    @Test
    void givenAnonymousUser_whenFindQuotes_thenReject() throws Exception {

        // WHEN
        int status = mockMvc.perform(get("/quotes"))
                .andReturn()
                .getResponse()
                .getStatus();

        // THEN
        assertAuthenticationRequired(status);
    }

    // =====================================
    // 3. PEDIDOS
    // =====================================

    @Test
    void givenAnonymousUser_whenFindOrders_thenReject() throws Exception {

        // WHEN
        int status = mockMvc.perform(get("/orders"))
                .andReturn()
                .getResponse()
                .getStatus();

        // THEN
        assertAuthenticationRequired(status);
    }

    // =====================================
    // 4. INVENTARIO
    // =====================================

    @Test
    void givenAnonymousUser_whenFindInventory_thenReject() throws Exception {

        // WHEN
        int status = mockMvc.perform(get("/inventory"))
                .andReturn()
                .getResponse()
                .getStatus();

        // THEN
        assertAuthenticationRequired(status);
    }

    // =====================================
    // 5. BÚSQUEDA DE PEDIDOS
    // =====================================

    @Test
    void givenAnonymousUser_whenSearchOrders_thenReject() throws Exception {

        // GIVEN
        String json = """
                {
                    "page": 0,
                    "size": 10
                }
                """;

        // WHEN
        int status = mockMvc.perform(post("/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn()
                .getResponse()
                .getStatus();

        // THEN
        assertAuthenticationRequired(status);
    }
}
