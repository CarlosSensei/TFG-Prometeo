package com.optiflow.crm.user;

import com.optiflow.crm.user.dto.CreateUserDto;
import com.optiflow.crm.user.dto.UserDto;
import com.optiflow.crm.user.mapper.UserMapper;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;
import com.optiflow.crm.user.model.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private UserService userService;
    private UserMapper userMapper;
    private MockMvc mockMvc;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        userService = mock(UserService.class);
        userMapper = mock(UserMapper.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        UserController controller = new UserController(userService, userMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter(
                                JsonMapper.builder().findAndAddModules().build()))
                .build();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private User createUser(Long id) {

        User user = new User();

        user.setId(id);
        user.setFirstName("Carlos");
        user.setLastName("García");
        user.setEmail("carlos@optiflow.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.SELLER);
        user.setActive(true);

        return user;
    }

    private UserDto createUserDto(Long id) {

        UserDto dto = new UserDto();

        dto.setId(id);
        dto.setFirstName("Carlos");
        dto.setLastName("García");
        dto.setEmail("carlos@optiflow.com");
        dto.setRole(UserRole.SELLER);
        dto.setActive(true);

        return dto;
    }

    private String validUserJson() {

        return """
                {
                    "firstName": "Carlos",
                    "lastName": "García",
                    "email": "carlos@optiflow.com",
                    "password": "Password123!",
                    "role": "SELLER"
                }
                """;
    }

    private void assertInvalidCreate(String json) throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService, userMapper);
    }

    // =====================================
    // 1. FIND ALL
    // =====================================

    @Test
    void givenUsers_whenFindAll_thenReturn200AndPage() throws Exception {

        // GIVEN
        User user1 = createUser(1L);
        User user2 = createUser(2L);

        UserDto dto1 = createUserDto(1L);
        UserDto dto2 = createUserDto(2L);

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userService.findAll(any(Pageable.class))).thenReturn(page);
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        // WHEN + THEN
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(userService).findAll(argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 10));
        verify(userMapper).toDto(user1);
        verify(userMapper).toDto(user2);
    }

    // =====================================
    // 2. EMPTY PAGE
    // =====================================

    @Test
    void givenNoUsers_whenFindAll_thenReturnEmptyPage() throws Exception {

        // GIVEN
        when(userService.findAll(any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        // WHEN + THEN
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verifyNoInteractions(userMapper);
    }

    // =====================================
    // 3. PAGINATION
    // =====================================

    @Test
    void givenPagination_whenFindAll_thenPassCorrectPageable() throws Exception {

        // GIVEN
        when(userService.findAll(any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(2, 5)));

        // WHEN + THEN
        mockMvc.perform(get("/users")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(userService).findAll(argThat(p ->
                        p.getPageNumber() == 2 && p.getPageSize() == 5));
    }

    // =====================================
    // 4. FIND BY ID
    // =====================================

    @Test
    void givenExistingUser_whenFindById_thenReturn200() throws Exception {

        // GIVEN
        User user = createUser(7L);
        UserDto dto = createUserDto(7L);

        when(userService.findById(7L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.lastName").value("García"))
                .andExpect(jsonPath("$.email")
                        .value("carlos@optiflow.com"))
                .andExpect(jsonPath("$.role").value("SELLER"))
                .andExpect(jsonPath("$.active").value(true));

        verify(userService).findById(7L);
        verify(userMapper).toDto(user);
    }

    // =====================================
    // 5. CREATE USER
    // =====================================

    @Test
    void givenValidDto_whenCreate_thenReturn201() throws Exception {

        // GIVEN
        User saved = createUser(9L);
        UserDto response = createUserDto(9L);

        when(userService.create(any(CreateUserDto.class))).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUserJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.email")
                        .value("carlos@optiflow.com"))
                .andExpect(jsonPath("$.role").value("SELLER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        // VERIFY
        verify(userService).create(argThat(dto ->
                        "Carlos".equals(dto.getFirstName())
                                && "García".equals(dto.getLastName())
                                && "carlos@optiflow.com".equals(dto.getEmail())
                                && "Password123!".equals(dto.getPassword())
                                && dto.getRole() == UserRole.SELLER));

        verify(userMapper).toDto(saved);
    }

    // =====================================
    // 6. DELETE USER
    // =====================================

    @Test
    void givenExistingUser_whenDelete_thenReturn204() throws Exception {

        // WHEN + THEN
        mockMvc.perform(delete("/users/9"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // VERIFY
        verify(userService).delete(9L);
        verifyNoInteractions(userMapper);
    }

    // =====================================
    // 7. FIRST NAME REQUIRED
    // =====================================

    @Test
    void givenMissingFirstName_whenCreate_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "lastName": "García",
                    "email": "carlos@optiflow.com",
                    "password": "Password123!",
                    "role": "SELLER"
                }
                """;

        // WHEN + THEN
        assertInvalidCreate(json);
    }

    // =====================================
    // 8. LAST NAME REQUIRED
    // =====================================

    @Test
    void givenBlankLastName_whenCreate_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "firstName": "Carlos",
                    "lastName": "   ",
                    "email": "carlos@optiflow.com",
                    "password": "Password123!",
                    "role": "SELLER"
                }
                """;

        // WHEN + THEN
        assertInvalidCreate(json);
    }

    // =====================================
    // 9. EMAIL FORMAT
    // =====================================

    @Test
    void givenInvalidEmail_whenCreate_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "firstName": "Carlos",
                    "lastName": "García",
                    "email": "invalid-email",
                    "password": "Password123!",
                    "role": "SELLER"
                }
                """;

        // WHEN + THEN
        assertInvalidCreate(json);
    }

    // =====================================
    // 10. PASSWORD LENGTH
    // =====================================

    @Test
    void givenShortPassword_whenCreate_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "firstName": "Carlos",
                    "lastName": "García",
                    "email": "carlos@optiflow.com",
                    "password": "123",
                    "role": "SELLER"
                }
                """;

        // WHEN + THEN
        assertInvalidCreate(json);
    }

    // =====================================
    // 11. ROLE REQUIRED
    // =====================================

    @Test
    void givenMissingRole_whenCreate_thenReturn400() throws Exception {

        // GIVEN
        String json = """
                {
                    "firstName": "Carlos",
                    "lastName": "García",
                    "email": "carlos@optiflow.com",
                    "password": "Password123!"
                }
                """;

        // WHEN + THEN
        assertInvalidCreate(json);
    }

    // =====================================
    // 12. NON NUMERIC ID
    // =====================================

    @Test
    void givenNonNumericId_whenFindById_thenReturn400() throws Exception {

        // WHEN + THEN
        mockMvc.perform(get("/users/abc")).andExpect(status().isBadRequest());

        verifyNoInteractions(userService, userMapper);
    }

    // =====================================
    // 13. PASSWORD NOT EXPOSED
    // =====================================

    @Test
    void givenExistingUser_whenFindById_thenDoNotExposePassword() throws Exception {

        // GIVEN
        User user = createUser(3L);
        UserDto dto = createUserDto(3L);

        when(userService.findById(3L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userMapper).toDto(user);
    }
}
