package com.optiflow.crm.client;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.mapper.ClientMapper;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.client.model.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests HTTP del controlador en modo standalone: no incluyen la cadena de Spring Security.
 * La capa de servicio y el mapper se sustituyen por mocks.
 */
class ClientControllerTest {

    private ClientService clientService;
    private ClientMapper clientMapper;
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {

        // 1. Inicializar los mocks de Mockito
        clientService = mock(ClientService.class);
        clientMapper = mock(ClientMapper.class);

        // 2. Configurar Jakarta Validation
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        // 3. Crear el controlador con las dependencias simuladas
        ClientController controller = new ClientController(clientService, clientMapper);

        // 4. Configurar MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                // Permitir parámetros Pageable
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                // Activar las validaciones del DTO
                .setValidator(validator)
                // Configurar Jackson 3 para Spring Boot 4
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .build();
    }

    @Test
    void givenClients_whenFindAll_thenReturn200AndPagedJson() throws Exception {

        // GIVEN
        Client client = new Client();
        client.setId(1L);
        client.setName("Ana");

        ClientDto dto = validClientDto();
        dto.setId(1L);
        dto.setName("Ana");

        Pageable pageable = PageRequest.of(0, 10);

        Page<Client> clientPage = new PageImpl<>(List.of(client), pageable, 1);

        when(clientService.findAll(any(Pageable.class))).thenReturn(clientPage);

        when(clientMapper.toDto(client)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/clients")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ana"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        // VERIFY
        verify(clientService).findAll(
                argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));

        verify(clientMapper).toDto(client);
    }

    @Test
    void givenSearchBody_whenSearch_thenReturn200() throws Exception {

        // GIVEN
        Client client = new Client();
        client.setId(2L);
        client.setName("Ana");

        ClientDto dto = validClientDto();
        dto.setId(2L);
        dto.setName("Ana");

        Pageable pageable = PageRequest.of(0, 10);

        Page<Client> resultPage = new PageImpl<>(List.of(client), pageable, 1);

        when(clientService.search(any(ClientSearchDto.class))).thenReturn(resultPage);

        when(clientMapper.toDto(client)).thenReturn(dto);

        String requestBody = """
            {
                "name": "Ana",
                "active": true,
                "page": 0,
                "size": 10
            }
            """;

        // WHEN + THEN
        mockMvc.perform(post("/clients/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Ana"))
                .andExpect(jsonPath("$.totalElements").value(1));

        // VERIFY
        verify(clientService).search(
                argThat(search -> "Ana".equals(search.getName())
                                && Boolean.TRUE.equals(search.getActive())
                                && search.getPage() == 0 && search.getSize() == 10));

        verify(clientMapper).toDto(client);
    }

    @Test
    void givenExistingId_whenFindById_thenReturnClient() throws Exception {
        Client client = new Client();
        client.setId(5L);
        ClientDto dto = validClientDto();
        dto.setId(5L);
        when(clientService.findById(5L)).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(dto);

        mockMvc.perform(get("/clients/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Ana"));

        verify(clientService).findById(5L);
    }

    @Test
    void givenValidClient_whenCreate_thenReturn201() throws Exception {
        Client entity = new Client();
        Client saved = new Client();
        saved.setId(9L);
        ClientDto response = validClientDto();
        response.setId(9L);
        when(clientMapper.toEntity(any(ClientDto.class))).thenReturn(entity);
        when(clientService.save(entity)).thenReturn(saved);
        when(clientMapper.toDto(saved)).thenReturn(response);

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.name").value("Ana"));

        verify(clientService).save(entity);
    }

    @Test
    void givenValidClient_whenUpdate_thenReturn200() throws Exception {
        Client entity = new Client();
        Client updated = new Client();
        updated.setId(9L);
        ClientDto response = validClientDto();
        response.setId(9L);
        when(clientMapper.toEntity(any(ClientDto.class))).thenReturn(entity);
        when(clientService.update(9L, entity)).thenReturn(updated);
        when(clientMapper.toDto(updated)).thenReturn(response);

        mockMvc.perform(put("/clients/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));

        verify(clientService).update(9L, entity);
    }

    @Test
    void givenExistingId_whenDelete_thenReturn204() throws Exception {
        mockMvc.perform(delete("/clients/9"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(clientService).delete(9L);
    }

    @Test
    void givenInvalidEmail_whenCreate_thenReturn400AndNeverSave() throws Exception {
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientJson().replace("ana@example.com", "correo-no-valido")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(clientService, clientMapper);
    }

    @Test
    void givenMissingBirthDate_whenCreate_thenReturn400AndNeverSave() throws Exception {

        // GIVEN
        String requestBody = """
            {
                "name": "Ana",
                "surname": "García",
                "lastSurname": "López",
                "Dni": "12345678Z",
                "phoneNumber": "600123456",
                "email": "ana.garcia@email.com",
                "address": "Carrer Mallorca 120",
                "city": "Barcelona",
                "postalCode": "08036",
                "active": true
            }
            """;

        // WHEN + THEN
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestBody)).andExpect(status().isBadRequest());

        // VERIFY
        verifyNoInteractions(clientService, clientMapper);
    }

    @Test
    void givenInvalidEmail_whenUpdate_thenReturn400AndNeverUpdate() throws Exception {
        mockMvc.perform(put("/clients/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientJson().replace("ana@example.com", "correo-no-valido")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(clientService, clientMapper);
    }

    @Test
    void givenNonNumericId_whenFindById_thenReturn400() throws Exception {
        mockMvc.perform(get("/clients/abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(clientService, clientMapper);
    }

    private static ClientDto validClientDto() {
        ClientDto dto = new ClientDto();
        dto.setName("Ana");
        dto.setSurname("García");
        dto.setDni("12345678Z");
        dto.setPhoneNumber("600111222");
        dto.setEmail("ana@example.com");
        dto.setBirthDate(LocalDate.of(1985, 5, 20));
        dto.setPostalCode("08036");
        return dto;
    }

    private static String validClientJson() {
        // El DTO publicado declara el campo como "Dni"; la propiedad JSON puede variar
        // si se normaliza el atributo y su getter en el código local.
        return """
                {
                  "name": "Ana",
                  "surname": "García",
                  "lastSurname": "López",
                  "Dni": "12345678Z",
                  "phoneNumber": "600111222",
                  "email": "ana@example.com",
                  "birthDate": "1985-05-20",
                  "postalCode": "08036"
                }
                """;
    }
}
