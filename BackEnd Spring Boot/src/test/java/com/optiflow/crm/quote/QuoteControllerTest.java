package com.optiflow.crm.quote;

import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.mapper.QuoteMapper;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteService;

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

class QuoteControllerTest {

    private QuoteService quoteService;
    private QuoteMapper quoteMapper;
    private MockMvc mockMvc;

    // =====================================
    // SETUP
    // =====================================

    @BeforeEach
    void setUp() {

        quoteService = mock(QuoteService.class);
        quoteMapper = mock(QuoteMapper.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        QuoteController controller = new QuoteController(quoteService, quoteMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .setValidator(validator)
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(
                                JsonMapper.builder().build())
                )
                .build();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Quote createQuote(Long id) {
        Quote quote = new Quote();
        quote.setId(id);
        return quote;
    }

    private QuoteDto createQuoteDto(Long id) {

        QuoteDto dto = new QuoteDto();

        dto.setId(id);
        dto.setClientId(7L);
        dto.setQuoteNumber("Q-2026-000001");
        dto.setNotes("Presupuesto de prueba");

        return dto;
    }

    private String validQuoteJson() {
        return """
                {
                    "clientId": 7,
                    "notes": "Presupuesto de prueba"
                }
                """;
    }

    // =====================================
    // FIND ALL
    // =====================================

    @Test
    void givenQuotes_whenFindAll_thenReturn200AndPagedJson() throws Exception {

        // GIVEN
        Quote quote = createQuote(1L);
        QuoteDto dto = createQuoteDto(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Quote> page = new PageImpl<>(List.of(quote), pageable, 1);

        when(quoteService.findAll(any(Pageable.class))).thenReturn(page);
        when(quoteMapper.toDto(quote)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/quotes")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].clientId").value(7))
                .andExpect(jsonPath("$.totalElements").value(1));

        // VERIFY
        verify(quoteService).findAll(argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 10));

        verify(quoteMapper).toDto(quote);
    }

    // =====================================
    // SEARCH
    // =====================================

    @Test
    void givenSearchBody_whenSearch_thenReturn200() throws Exception {

        // GIVEN
        Quote quote = createQuote(2L);
        QuoteDto dto = createQuoteDto(2L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Quote> page = new PageImpl<>(List.of(quote), pageable, 1);

        when(quoteService.search(any(QuoteSearchDto.class))).thenReturn(page);
        when(quoteMapper.toDto(quote)).thenReturn(dto);

        String json = """
                {
                    "clientId": 7,
                    "page": 0,
                    "size": 10
                }
                """;

        // WHEN + THEN
        mockMvc.perform(post("/quotes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(quoteService).search(argThat(search ->
                        Long.valueOf(7L).equals(search.getClientId())
                                && Integer.valueOf(0).equals(search.getPage())
                                && Integer.valueOf(10).equals(search.getSize()))
        );

        verify(quoteMapper).toDto(quote);
    }

    // =====================================
    // FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturn200() throws Exception {

        // GIVEN
        Quote quote = createQuote(5L);
        QuoteDto dto = createQuoteDto(5L);

        when(quoteService.findById(5L)).thenReturn(quote);
        when(quoteMapper.toDto(quote)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/quotes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.clientId").value(7))
                .andExpect(jsonPath("$.quoteNumber")
                        .value("Q-2026-000001"));

        verify(quoteService).findById(5L);
        verify(quoteMapper).toDto(quote);
    }

    // =====================================
    // CREATE
    // =====================================

    @Test
    void givenValidQuote_whenCreate_thenReturn201() throws Exception {

        // GIVEN
        Quote saved = createQuote(9L);
        QuoteDto responseDto = createQuoteDto(9L);

        when(quoteService.create(any(QuoteDto.class))).thenReturn(saved);
        when(quoteMapper.toDto(saved)).thenReturn(responseDto);

        // WHEN + THEN
        mockMvc.perform(post("/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validQuoteJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.clientId").value(7));

        // VERIFY
        verify(quoteService).create(argThat(dto ->
                        Long.valueOf(7L).equals(dto.getClientId())));

        verify(quoteMapper).toDto(saved);
    }

    // =====================================
    // UPDATE
    // =====================================

    @Test
    void givenValidQuote_whenUpdate_thenReturn200() throws Exception {

        // GIVEN
        Quote updated = createQuote(9L);
        QuoteDto responseDto = createQuoteDto(9L);

        when(quoteService.update(eq(9L), any(QuoteDto.class))).thenReturn(updated);
        when(quoteMapper.toDto(updated)).thenReturn(responseDto);

        // WHEN + THEN
        mockMvc.perform(put("/quotes/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validQuoteJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.clientId").value(7));

        verify(quoteService).update(eq(9L), any(QuoteDto.class));
    }

    // =====================================
    // DELETE
    // =====================================

    @Test
    void givenExistingId_whenDelete_thenReturn204() throws Exception {

        // WHEN + THEN
        mockMvc.perform(delete("/quotes/9"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // VERIFY
        verify(quoteService).delete(9L);
        verifyNoInteractions(quoteMapper);
    }

    // =====================================
    // FIND BY CLIENT
    // =====================================

    @Test
    void givenClientId_whenFindByClient_thenReturnPage() throws Exception {

        // GIVEN
        Quote quote = createQuote(3L);
        QuoteDto dto = createQuoteDto(3L);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Quote> page = new PageImpl<>(List.of(quote), pageable, 1);

        when(quoteService.findByClientId(eq(7L), any(Pageable.class))).thenReturn(page);
        when(quoteMapper.toDto(quote)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/quotes/client/7")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(quoteService).findByClientId(eq(7L), argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 5));

        verify(quoteMapper).toDto(quote);
    }

    // =====================================
    // VALIDACIÓN: CLIENTE OBLIGATORIO
    // =====================================

    @Test
    void givenMissingClientId_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "notes": "Presupuesto sin cliente"
                }
                """;

        mockMvc.perform(post("/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());

        verifyNoInteractions(quoteService, quoteMapper);
    }

    // =====================================
    // VALIDACIÓN: CLIENTE NEGATIVO
    // =====================================

    @Test
    void givenNegativeClientId_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "clientId": -5,
                    "notes": "Cliente inválido"
                }
                """;

        mockMvc.perform(post("/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());

        verifyNoInteractions(quoteService, quoteMapper);
    }

    // =====================================
    // VALIDACIÓN: DESCUENTO NEGATIVO
    // =====================================

    @Test
    void givenNegativeDiscount_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "clientId": 7,
                    "discount": -10.00
                }
                """;

        mockMvc.perform(post("/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());

        verifyNoInteractions(quoteService, quoteMapper);
    }

    // =====================================
    // ID NO NUMÉRICO
    // =====================================

    @Test
    void givenNonNumericId_whenFindById_thenReturn400() throws Exception {

        mockMvc.perform(get("/quotes/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(quoteService, quoteMapper);
    }
}
