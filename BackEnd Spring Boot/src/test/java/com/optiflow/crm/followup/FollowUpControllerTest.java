package com.optiflow.crm.followup;

import com.optiflow.crm.followup.dto.CompleteFollowUpDto;
import com.optiflow.crm.followup.dto.CreateFollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.mapper.FollowUpMapper;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpService;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;

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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FollowUpControllerTest {

    private FollowUpService followUpService;
    private FollowUpMapper followUpMapper;
    private MockMvc mockMvc;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        followUpService = mock(FollowUpService.class);
        followUpMapper = mock(FollowUpMapper.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        FollowUpController controller = new FollowUpController(followUpService, followUpMapper);

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

    private FollowUp createFollowUp(Long id) {

        FollowUp followUp = new FollowUp();

        followUp.setId(id);
        followUp.setType(FollowUpType.CALL);
        followUp.setStatus(FollowUpStatus.PENDING);
        followUp.setScheduledAt(LocalDateTime
                .of(2026, 11, 10, 10, 0));
        followUp.setNotes("Llamar al cliente");
        followUp.setActive(true);

        return followUp;
    }

    private FollowUpDto createFollowUpDto(Long id) {

        FollowUpDto dto = new FollowUpDto();

        dto.setId(id);
        dto.setQuoteId(5L);
        dto.setType(FollowUpType.CALL);
        dto.setStatus(FollowUpStatus.PENDING);
        dto.setScheduledAt(LocalDateTime
                .of(2026, 11, 10, 10, 0));
        dto.setNotes("Llamar al cliente");
        dto.setActive(true);

        return dto;
    }

    private String validFollowUpJson() {

        return """
                {
                    "quoteId": 5,
                    "type": "CALL",
                    "scheduledAt": "2026-11-10T10:00:00",
                    "notes": "Llamar al cliente"
                }
                """;
    }

    private void assertInvalidCreate(String json) throws Exception {

        mockMvc.perform(post("/quotes/5/followups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(followUpService, followUpMapper);
    }

    // =====================================
    // 1. FIND ALL
    // =====================================

    @Test
    void givenFollowUps_whenFindAll_thenReturn200AndPage() throws Exception {

        // GIVEN
        FollowUp followUp = createFollowUp(1L);
        FollowUpDto dto = createFollowUpDto(1L);

        Pageable pageable = PageRequest.of(0, 10);

        Page<FollowUp> page = new PageImpl<>(List.of(followUp), pageable, 1);

        when(followUpService.findAll(any(Pageable.class))).thenReturn(page);
        when(followUpMapper.toDto(followUp)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/followups")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].quoteId").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(followUpService).findAll(argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 10));
        verify(followUpMapper).toDto(followUp);
    }

    // =====================================
    // 2. SEARCH
    // =====================================

    @Test
    void givenSearchCriteria_whenSearch_thenReturn200() throws Exception {

        // GIVEN
        FollowUp followUp = createFollowUp(2L);
        FollowUpDto dto = createFollowUpDto(2L);

        Page<FollowUp> page = new PageImpl<>(List.of(followUp),
                PageRequest.of(0, 10), 1);

        when(followUpService.search(any(FollowUpSearchDto.class))).thenReturn(page);
        when(followUpMapper.toDto(followUp)).thenReturn(dto);

        String json = """
                {
                    "quoteId": 5,
                    "status": "PENDING",
                    "page": 0,
                    "size": 10
                }
                """;

        // WHEN + THEN
        mockMvc.perform(post("/followups/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(1));

        // VERIFY
        verify(followUpService).search(argThat(search ->
                        Long.valueOf(5L).equals(search.getQuoteId())
                                && search.getStatus() == FollowUpStatus.PENDING
                                && Boolean.TRUE.equals(search.getActive())
                                && Integer.valueOf(0).equals(search.getPage())
                                && Integer.valueOf(10).equals(search.getSize())));

        verify(followUpMapper).toDto(followUp);
    }

    // =====================================
    // 3. FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturn200() throws Exception {

        // GIVEN
        FollowUp followUp = createFollowUp(7L);
        FollowUpDto dto = createFollowUpDto(7L);

        when(followUpService.findById(7L)).thenReturn(followUp);
        when(followUpMapper.toDto(followUp)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/followups/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.type").value("CALL"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(followUpService).findById(7L);
        verify(followUpMapper).toDto(followUp);
    }

    // =====================================
    // 4. FIND BY QUOTE
    // =====================================

    @Test
    void givenQuoteId_whenFindByQuote_thenReturnPage() throws Exception {

        // GIVEN
        FollowUp followUp = createFollowUp(3L);
        FollowUpDto dto = createFollowUpDto(3L);

        Page<FollowUp> page = new PageImpl<>(List.of(followUp),
                PageRequest.of(0, 5), 1);

        when(followUpService.findByQuoteId(eq(5L), any(Pageable.class)))
                .thenReturn(page);
        when(followUpMapper.toDto(followUp)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/quotes/5/followups")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].quoteId").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(followUpService).findByQuoteId(eq(5L), argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 5));
    }

    // =====================================
    // 5. CREATE
    // =====================================

    @Test
    void givenValidDto_whenCreate_thenReturn201() throws Exception {

        // GIVEN
        FollowUp saved = createFollowUp(9L);
        FollowUpDto response = createFollowUpDto(9L);

        when(followUpService.create(eq(5L), any(CreateFollowUpDto.class)))
                .thenReturn(saved);
        when(followUpMapper.toDto(saved)).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(post("/quotes/5/followups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validFollowUpJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.quoteId").value(5))
                .andExpect(jsonPath("$.type").value("CALL"));

        // VERIFY
        verify(followUpService).create(eq(5L), argThat(dto ->
                        dto.getType() == FollowUpType.CALL
                                && "Llamar al cliente".equals(dto.getNotes())
                                && LocalDateTime.of(2026, 11, 10, 10, 0)
                                .equals(dto.getScheduledAt())));

        verify(followUpMapper).toDto(saved);
    }

    // =====================================
    // 6. UPDATE
    // =====================================

    @Test
    void givenValidDto_whenUpdate_thenReturn200() throws Exception {

        // GIVEN
        FollowUp updated = createFollowUp(9L);
        FollowUpDto response = createFollowUpDto(9L);

        when(followUpService.update(eq(9L), any(FollowUpDto.class)))
                .thenReturn(updated);
        when(followUpMapper.toDto(updated)).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(put("/followups/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validFollowUpJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.notes")
                        .value("Llamar al cliente"));

        verify(followUpService).update(eq(9L), argThat(dto ->
                        dto.getType() == FollowUpType.CALL
                                && Long.valueOf(5L).equals(dto.getQuoteId())));
    }

    // =====================================
    // 7. COMPLETE
    // =====================================

    @Test
    void givenValidCompleteDto_whenComplete_thenReturn200() throws Exception {

        // GIVEN
        FollowUp completed = createFollowUp(9L);
        completed.setStatus(FollowUpStatus.COMPLETED);

        FollowUpDto response = createFollowUpDto(9L);
        response.setStatus(FollowUpStatus.COMPLETED);
        response.setResult("Cliente interesado");

        when(followUpService.complete(eq(9L), any(CompleteFollowUpDto.class)))
                .thenReturn(completed);
        when(followUpMapper.toDto(completed)).thenReturn(response);

        String json = """
                {
                    "result": "Cliente interesado",
                    "notes": "Enviar presupuesto revisado"
                }
                """;

        // WHEN + THEN
        mockMvc.perform(patch("/followups/9/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result")
                        .value("Cliente interesado"));

        // VERIFY
        verify(followUpService).complete(eq(9L), argThat(dto ->
                        "Cliente interesado".equals(dto.getResult())
                                && "Enviar presupuesto revisado".equals(dto.getNotes())));
    }

    // =====================================
    // 8. DELETE
    // =====================================

    @Test
    void givenExistingId_whenDelete_thenReturn204() throws Exception {

        // WHEN + THEN
        mockMvc.perform(delete("/followups/9"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // VERIFY
        verify(followUpService).delete(9L);
        verifyNoInteractions(followUpMapper);
    }

    // =====================================
    // 9. MISSING TYPE
    // =====================================

    @Test
    void givenMissingType_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "quoteId": 5,
                    "scheduledAt": "2026-11-10T10:00:00"
                }
                """;

        assertInvalidCreate(json);
    }

    // =====================================
    // 10. MISSING SCHEDULED DATE
    // =====================================

    @Test
    void givenMissingScheduledAt_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "quoteId": 5,
                    "type": "CALL"
                }
                """;

        assertInvalidCreate(json);
    }

    // =====================================
    // 11. MISSING QUOTE ID IN DTO
    // =====================================

    @Test
    void givenMissingQuoteIdInBody_whenCreate_thenUsePathVariable() throws Exception {

        // GIVEN
        FollowUp saved = createFollowUp(9L);
        FollowUpDto response = createFollowUpDto(9L);

        when(followUpService.create(eq(5L), any(CreateFollowUpDto.class)))
                .thenReturn(saved);
        when(followUpMapper.toDto(saved)).thenReturn(response);

        String json = """
            {
                "type": "CALL",
                "scheduledAt": "2026-11-10T10:00:00"
            }
            """;

        // WHEN + THEN
        mockMvc.perform(post("/quotes/5/followups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quoteId").value(5));

        verify(followUpService).create(eq(5L), argThat(dto ->
                        dto.getType() == FollowUpType.CALL && dto.getScheduledAt() != null));
    }

    // =====================================
    // 12. DIFFERENT QUOTE ID
    // =====================================

    @Test
    void givenDifferentQuoteIdInPath_whenCreate_thenUsePathId()
            throws Exception {

        // GIVEN
        FollowUp saved = createFollowUp(9L);
        FollowUpDto response = createFollowUpDto(9L);
        response.setQuoteId(8L);

        when(followUpService.create(eq(8L), any(CreateFollowUpDto.class)))
                .thenReturn(saved);
        when(followUpMapper.toDto(saved)).thenReturn(response);

        String json = """
            {
                "type": "EMAIL",
                "scheduledAt": "2026-11-10T10:00:00"
            }
            """;

        // WHEN + THEN
        mockMvc.perform(post("/quotes/8/followups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quoteId").value(8));

        verify(followUpService).create(eq(8L), argThat(dto ->
                dto.getType() == FollowUpType.EMAIL));
        verify(followUpService, never()).create(eq(5L), any(CreateFollowUpDto.class));
    }

    // =====================================
    // 13. NOTES TOO LONG
    // =====================================

    @Test
    void givenLongNotes_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "quoteId": 5,
                    "type": "CALL",
                    "scheduledAt": "2026-11-10T10:00:00",
                    "notes": "%s"
                }
                """.formatted("A".repeat(1001));

        assertInvalidCreate(json);
    }

    // =====================================
    // 14. COMPLETE RESULT TOO LONG
    // =====================================

    @Test
    void givenLongResult_whenComplete_thenReturn400() throws Exception {

        String json = """
                {
                    "result": "%s"
                }
                """.formatted("A".repeat(501));

        // WHEN + THEN
        mockMvc.perform(patch("/followups/9/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(followUpService, followUpMapper);
    }

    // =====================================
    // 15. NON-NUMERIC ID
    // =====================================

    @Test
    void givenNonNumericId_whenFindById_thenReturn400() throws Exception {

        // WHEN + THEN
        mockMvc.perform(get("/followups/abc")).andExpect(status().isBadRequest());

        verifyNoInteractions(followUpService, followUpMapper);
    }
}
