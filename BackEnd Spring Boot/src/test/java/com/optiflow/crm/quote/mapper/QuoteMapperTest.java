
package com.optiflow.crm.quote.mapper;

import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.dto.QuoteItemDto;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteMapperTest {

    @Mock
    private QuoteItemMapper quoteItemMapper;

    private QuoteMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new QuoteMapper(quoteItemMapper);
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Quote completeQuote() {

        Quote quote = new Quote();
        quote.setId(1L);
        quote.setQuoteNumber("Q-2026-000001");

        Client client = new Client();
        client.setId(7L);
        quote.setClient(client);

        quote.setStatus(QuoteStatus.DRAFT);
        quote.setQuoteDate(LocalDate.of(2026, 10, 10));
        quote.setExpirationDate(LocalDate.of(2026, 11, 9));

        quote.setSubtotal(new BigDecimal("100.00"));
        quote.setDiscount(new BigDecimal("10.00"));
        quote.setTaxRate(new BigDecimal("21.00"));
        quote.setTaxAmount(new BigDecimal("18.90"));
        quote.setTotal(new BigDecimal("108.90"));

        quote.setNotes("Presupuesto de prueba");
        quote.setActive(true);

        quote.setCreatedAt(
                LocalDateTime.of(2026, 10, 10, 10, 0));

        quote.setUpdatedAt(
                LocalDateTime.of(2026, 10, 11, 12, 0));

        return quote;
    }

    private QuoteDto completeDto() {

        QuoteDto dto = new QuoteDto();

        dto.setId(10L);
        dto.setQuoteNumber("Q-2026-000010");
        dto.setClientId(7L);
        dto.setSellerId(3L);

        dto.setStatus(QuoteStatus.DRAFT);
        dto.setQuoteDate(LocalDate.of(2026, 10, 10));
        dto.setExpirationDate(LocalDate.of(2026, 11, 9));

        dto.setSubtotal(new BigDecimal("200.00"));
        dto.setDiscount(new BigDecimal("20.00"));
        dto.setTaxRate(new BigDecimal("21.00"));
        dto.setTaxAmount(new BigDecimal("37.80"));
        dto.setTotal(new BigDecimal("217.80"));

        dto.setNotes("Cliente preferente");
        dto.setActive(false);

        dto.setCreatedAt(
                LocalDateTime.of(2026, 10, 1, 9, 0));

        dto.setUpdatedAt(
                LocalDateTime.of(2026, 10, 5, 16, 0));

        return dto;
    }

    // =====================================
    // TO DTO
    // =====================================

    @Test
    void givenCompleteQuote_whenToDto_thenMapFields() {

        // GIVEN
        Quote quote = completeQuote();

        // WHEN
        QuoteDto dto = mapper.toDto(quote);

        // THEN
        assertAll("Quote -> QuoteDto",
                () -> assertEquals(quote.getId(), dto.getId()),
                () -> assertEquals(quote.getQuoteNumber(), dto.getQuoteNumber()),
                () -> assertEquals(7L, dto.getClientId()),
                () -> assertEquals(quote.getStatus(), dto.getStatus()),
                () -> assertEquals(quote.getQuoteDate(), dto.getQuoteDate()),
                () -> assertEquals(quote.getExpirationDate(), dto.getExpirationDate()),
                () -> assertEquals(quote.getSubtotal(), dto.getSubtotal()),
                () -> assertEquals(quote.getDiscount(), dto.getDiscount()),
                () -> assertEquals(quote.getTaxRate(), dto.getTaxRate()),
                () -> assertEquals(quote.getTaxAmount(), dto.getTaxAmount()),
                () -> assertEquals(quote.getTotal(), dto.getTotal()),
                () -> assertEquals(quote.getNotes(), dto.getNotes()),
                () -> assertEquals(quote.getActive(), dto.getActive()),
                () -> assertEquals(quote.getCreatedAt(), dto.getCreatedAt()),
                () -> assertEquals(quote.getUpdatedAt(), dto.getUpdatedAt())
        );

        assertTrue(dto.getItems().isEmpty());

        verifyNoInteractions(quoteItemMapper);
    }

    @Test
    void givenQuoteWithoutClient_whenToDto_thenClientIdNull() {

        // GIVEN
        Quote quote = new Quote();
        quote.setClient(null);

        // WHEN
        QuoteDto dto = mapper.toDto(quote);

        // THEN
        assertNull(dto.getClientId());
        assertNotNull(dto.getItems());

        verifyNoInteractions(quoteItemMapper);
    }

    @Test
    void givenQuoteWithItems_whenToDto_thenMapItems() {

        // GIVEN
        Quote quote = completeQuote();
        QuoteItem item1 = new QuoteItem();
        QuoteItem item2 = new QuoteItem();
        QuoteItemDto itemDto1 = new QuoteItemDto();
        itemDto1.setId(101L);
        QuoteItemDto itemDto2 = new QuoteItemDto();
        itemDto2.setId(102L);
        quote.setItems(List.of(item1, item2));

        when(quoteItemMapper.toDto(item1)).thenReturn(itemDto1);
        when(quoteItemMapper.toDto(item2)).thenReturn(itemDto2);

        // WHEN
        QuoteDto dto = mapper.toDto(quote);

        // THEN
        assertEquals(2, dto.getItems().size());

        assertSame(itemDto1, dto.getItems().get(0));
        assertSame(itemDto2, dto.getItems().get(1));

        verify(quoteItemMapper).toDto(item1);
        verify(quoteItemMapper).toDto(item2);

        verifyNoMoreInteractions(quoteItemMapper);
    }

    @Test
    void givenEmptyItems_whenToDto_thenReturnEmptyList() {

        // GIVEN
        Quote quote = completeQuote();
        quote.setItems(List.of());

        // WHEN
        QuoteDto dto = mapper.toDto(quote);

        // THEN
        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());

        verifyNoInteractions(quoteItemMapper);
    }

    // =====================================
    // TO ENTITY
    // =====================================

    @Test
    void givenCompleteDto_whenToEntity_thenMapFields() {

        // GIVEN
        QuoteDto dto = completeDto();

        // WHEN
        Quote quote = mapper.toEntity(dto);

        // THEN
        assertNotNull(quote);

        assertAll("QuoteDto -> Quote",
                () -> assertEquals(dto.getStatus(), quote.getStatus()),
                () -> assertEquals(dto.getQuoteDate(), quote.getQuoteDate()),
                () -> assertEquals(dto.getExpirationDate(), quote.getExpirationDate()),
                () -> assertEquals(dto.getSubtotal(), quote.getSubtotal()),
                () -> assertEquals(dto.getDiscount(), quote.getDiscount()),
                () -> assertEquals(dto.getTaxRate(), quote.getTaxRate()),
                () -> assertEquals(dto.getNotes(), quote.getNotes())
        );

        verifyNoInteractions(quoteItemMapper);
    }

    @Test
    void givenDtoWithControlledFields_whenToEntity_thenIgnoreThem() {

        // GIVEN
        QuoteDto dto = completeDto();

        // WHEN
        Quote quote = mapper.toEntity(dto);

        // THEN
        assertNull(quote.getId());
        assertNull(quote.getQuoteNumber());
        assertNull(quote.getClient());
        assertNull(quote.getSeller());

        // Los artículos los construye QuoteServiceImpl
        assertTrue(quote.getItems().isEmpty());

        // Los importes calculados no se copian
        assertEquals(BigDecimal.ZERO, quote.getTaxAmount());
        assertEquals(BigDecimal.ZERO, quote.getTotal());

        // Valores iniciales de la entidad
        assertEquals(true, quote.getActive());

        assertNull(quote.getCreatedAt());
        assertNull(quote.getUpdatedAt());

        verifyNoInteractions(quoteItemMapper);
    }

    // =====================================
    // UPDATE ENTITY
    // =====================================

    @Test
    void givenDto_whenUpdateEntity_thenUpdateEditableFields() {

        // GIVEN
        QuoteDto dto = completeDto();
        Quote existing = completeQuote();

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertAll("Campos editables",
                () -> assertEquals(dto.getStatus(), existing.getStatus()),
                () -> assertEquals(dto.getQuoteDate(), existing.getQuoteDate()),
                () -> assertEquals(dto.getExpirationDate(), existing.getExpirationDate()),
                () -> assertEquals(dto.getSubtotal(), existing.getSubtotal()),
                () -> assertEquals(dto.getDiscount(), existing.getDiscount()),
                () -> assertEquals(dto.getTaxRate(), existing.getTaxRate()),
                () -> assertEquals(dto.getNotes(), existing.getNotes())
        );

        verifyNoInteractions(quoteItemMapper);
    }

    @Test
    void givenDto_whenUpdateEntity_thenPreserveControlledFields() {

        // GIVEN
        QuoteDto dto = completeDto();
        Quote existing = completeQuote();
        Client originalClient = existing.getClient();
        User seller = new User();
        existing.setSeller(seller);
        QuoteItem item = new QuoteItem();
        existing.setItems(List.of(item));
        Long originalId = existing.getId();
        String originalNumber = existing.getQuoteNumber();
        BigDecimal originalTaxAmount = existing.getTaxAmount();
        BigDecimal originalTotal = existing.getTotal();
        LocalDateTime originalCreated = existing.getCreatedAt();
        LocalDateTime originalUpdated = existing.getUpdatedAt();

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertAll("Campos protegidos",
                () -> assertEquals(originalId, existing.getId()),
                () -> assertEquals(originalNumber, existing.getQuoteNumber()),
                () -> assertSame(originalClient, existing.getClient()),
                () -> assertSame(seller, existing.getSeller()),
                () -> assertSame(item, existing.getItems().getFirst()),
                () -> assertEquals(originalTaxAmount, existing.getTaxAmount()),
                () -> assertEquals(originalTotal, existing.getTotal()),
                () -> assertEquals(true, existing.getActive()),
                () -> assertEquals(originalCreated, existing.getCreatedAt()),
                () -> assertEquals(originalUpdated, existing.getUpdatedAt())
        );

        verifyNoInteractions(quoteItemMapper);
    }
}
