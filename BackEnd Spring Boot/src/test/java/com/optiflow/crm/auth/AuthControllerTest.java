package com.optiflow.crm.auth;

import com.optiflow.crm.auth.dto.LoginRequest;
import com.optiflow.crm.auth.dto.LoginResponse;
import com.optiflow.crm.user.dto.UserDto;
import com.optiflow.crm.user.mapper.UserMapper;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private AuthService authService;
    private CurrentUserService currentUserService;
    private UserMapper userMapper;

    private MockMvc mockMvc;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        authService = mock(AuthService.class);
        currentUserService = mock(CurrentUserService.class);
        userMapper = mock(UserMapper.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        AuthController controller = new AuthController(
                authService,
                currentUserService,
                userMapper
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter(
                                JsonMapper.builder().findAndAddModules().build()))
                .build();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private String validLoginJson() {

        return """
                {
                    "email": "carlos@optiflow.com",
                    "password": "Password123!"
                }
                """;
    }

    private LoginResponse createLoginResponse() {

        return new LoginResponse(
                "jwt-token-example",
                "Bearer",
                1L,
                "carlos@optiflow.com",
                "Carlos",
                "García",
                UserRole.SELLER
        );
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

    private UserDto createUserDto() {

        UserDto dto = new UserDto();

        dto.setId(1L);
        dto.setFirstName("Carlos");
        dto.setLastName("García");
        dto.setEmail("carlos@optiflow.com");
        dto.setRole(UserRole.SELLER);
        dto.setActive(true);

        return dto;
    }

    private void assertInvalidLogin(String json) throws Exception {

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());

        // Las peticiones inválidas no llegan al servicio.
        verifyNoInteractions(authService, currentUserService, userMapper);
    }

    // =====================================
    // 1. LOGIN CORRECTO
    // =====================================

    @Test
    void givenValidCredentials_whenLogin_thenReturn200AndToken() throws Exception {

        // GIVEN
        LoginResponse response = createLoginResponse();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginJson())).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-example"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("carlos@optiflow.com"))
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.lastName").value("García"))
                .andExpect(jsonPath("$.role").value("SELLER"));

        verify(authService).login(argThat(request ->
                        "carlos@optiflow.com".equals(request.getEmail())
                                && "Password123!".equals(request.getPassword())));

        verifyNoInteractions(currentUserService, userMapper);
    }

    // =====================================
    // 2. LOGIN CON EMAIL EN MAYÚSCULAS
    // =====================================


    @Test
    void givenUppercaseEmail_whenLogin_thenPassOriginalToService() throws Exception {

        // GIVEN
        String json = """
            {
                "email": "CARLOS@OPTIFLOW.COM",
                "password": "Password123!"
            }
            """;

        when(authService.login(any(LoginRequest.class))).thenReturn(createLoginResponse());

        // WHEN + THEN
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-example"));

        // VERIFY
        verify(authService).login(argThat(request ->
                        "CARLOS@OPTIFLOW.COM".equals(request.getEmail())));

        // AuthService se encarga de pasar el email
        // a minúsculas antes de autenticar.
    }


    // =====================================
    // 3. LOGIN SIN EMAIL
    // =====================================

    @Test
    void givenMissingEmail_whenLogin_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "password": "Password123!"
                }
                """;

        // WHEN + THEN
        assertInvalidLogin(json);
    }

    // =====================================
    // 4. EMAIL INVÁLIDO
    // =====================================

    @Test
    void givenInvalidEmail_whenLogin_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "email": "invalid-email",
                    "password": "Password123!"
                }
                """;

        // WHEN + THEN
        assertInvalidLogin(json);
    }

    // =====================================
    // 5. LOGIN SIN PASSWORD
    // =====================================

    @Test
    void givenMissingPassword_whenLogin_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "email": "carlos@optiflow.com"
                }
                """;

        // WHEN + THEN
        assertInvalidLogin(json);
    }

    // =====================================
    // 6. PASSWORD EN BLANCO
    // =====================================

    @Test
    void givenBlankPassword_whenLogin_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "email": "carlos@optiflow.com",
                    "password": "   "
                }
                """;

        // WHEN + THEN
        assertInvalidLogin(json);
    }

    // =====================================
    // 7. LOGIN CON JSON VACÍO
    // =====================================

    @Test
    void givenEmptyJson_whenLogin_thenReturn400() throws Exception {

        // GIVEN
        String json = "{}";

        // WHEN + THEN
        assertInvalidLogin(json);
    }

    // =====================================
    // 8. LOGIN CON JSON MAL FORMADO
    // =====================================

    @Test
    void givenMalformedJson_whenLogin_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "email": "carlos@optiflow.com",
                    "password":
                }
                """;

        // WHEN + THEN
        assertInvalidLogin(json);
    }

    // =====================================
    // 9. LOGIN NO EXPONE PASSWORD
    // =====================================

    @Test
    void givenValidLogin_whenReturnResponse_thenDoNotExposePassword() throws Exception {

        // GIVEN
        when(authService.login(any(LoginRequest.class))).thenReturn(createLoginResponse());

        // WHEN + THEN
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginJson())).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(authService).login(any(LoginRequest.class));
    }

    // =====================================
    // 10. ME DEVUELVE USUARIO ACTUAL
    // =====================================

    @Test
    void givenCurrentUser_whenGetMe_thenReturn200AndUserData() throws Exception {

        // GIVEN
        User user = createUser();
        UserDto dto = createUserDto();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.lastName").value("García"))
                .andExpect(jsonPath("$.email").value("carlos@optiflow.com"))
                .andExpect(jsonPath("$.role").value("SELLER"))
                .andExpect(jsonPath("$.active").value(true));

        verify(currentUserService).getCurrentUser();
        verify(userMapper).toDto(user);

        verifyNoInteractions(authService);
    }

    // =====================================
    // 11. ME NO EXPONE PASSWORD
    // =====================================

    @Test
    void givenCurrentUser_whenGetMe_thenDoNotExposePassword() throws Exception {

        // GIVEN
        User user = createUser();
        UserDto dto = createUserDto();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(currentUserService).getCurrentUser();
        verify(userMapper).toDto(user);
    }
}
