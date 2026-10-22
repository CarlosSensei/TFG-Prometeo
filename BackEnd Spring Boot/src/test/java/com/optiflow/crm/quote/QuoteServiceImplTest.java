package com.optiflow.crm.quote;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.quote.domain.QuoteDomainService;
import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.dto.QuoteItemDto;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.mapper.QuoteMapper;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.shared.exception.ClientNotFoundException;
import com.optiflow.crm.shared.exception.InventoryItemNotFoundException;
import com.optiflow.crm.shared.exception.QuoteNotFoundException;
import com.optiflow.crm.user.model.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceImplTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private QuoteMapper quoteMapper;

    @Mock
    private QuoteDomainService quoteDomainService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private QuoteServiceImpl service;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private QuoteDto quoteDto() {
        QuoteDto dto = new QuoteDto();
        dto.setClientId(7L);
        return dto;
    }

    private void prepareCreation(QuoteDto dto, Quote quote) {

        when(clientRepository.findById(7L)).thenReturn(Optional.of(new Client()));

        when(quoteMapper.toEntity(dto)).thenReturn(quote);

        when(currentUserService.getCurrentUser()).thenReturn(new User());
    }

    // =====================================
    // FIND ALL
    // =====================================

    @Test
    void givenActiveQuotes_whenFindAll_thenReturnPage() {

        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);

        Page<Quote> page = new PageImpl<>(List.of(new Quote()), pageable, 1);

        when(quoteRepository.findByActiveTrue(pageable)).thenReturn(page);

        // WHEN
        Page<Quote> result = service.findAll(pageable);

        // THEN
        assertSame(page, result);

        verify(quoteRepository).findByActiveTrue(pageable);
    }

    // =====================================
    // FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturnQuote() {

        Quote quote = new Quote();

        when(quoteRepository.findById(3L)).thenReturn(Optional.of(quote));

        Quote result = service.findById(3L);

        assertSame(quote, result);

        verify(quoteRepository).findById(3L);
    }

    @Test
    void givenMissingId_whenFindById_thenThrow() {

        when(quoteRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(QuoteNotFoundException.class, () -> service.findById(3L));
    }

    // =====================================
    // FIND BY CLIENT
    // =====================================

    @Test
    void givenClientId_whenFindByClientId_thenReturnActivePage() {

        Pageable pageable = PageRequest.of(1, 5);

        Page<Quote> page = new PageImpl<>(List.of());

        when(quoteRepository.findByClientIdAndActiveTrue(7L, pageable)).thenReturn(page);

        Page<Quote> result = service.findByClientId(7L, pageable);

        assertSame(page, result);

        verify(quoteRepository).findByClientIdAndActiveTrue(7L, pageable);
    }

    // =====================================
    // SEARCH
    // =====================================

    @Test
    void givenSearch_whenSearch_thenUsePageAndSort() {

        QuoteSearchDto search = new QuoteSearchDto();

        search.setPage(2);
        search.setSize(5);
        search.setSortBy("quoteDate");
        search.setSortDirection("asc");

        when(quoteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        // WHEN
        service.search(search);

        // THEN
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(quoteRepository).findAll(any(Specification.class), captor.capture());

        Pageable actual = captor.getValue();

        assertEquals(2, actual.getPageNumber());
        assertEquals(5, actual.getPageSize());

        assertTrue(actual.getSort().getOrderFor("quoteDate").isAscending());
    }

    // =====================================
    // CREATE
    // =====================================

    @Test
    void givenValidClient_whenCreate_thenSetRelationsAndGeneratedNumber() {

        // GIVEN
        QuoteDto dto = quoteDto();
        Quote quote = new Quote();

        Client client = new Client();
        User seller = new User();

        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(quoteMapper.toEntity(dto)).thenReturn(quote);
        when(currentUserService.getCurrentUser()).thenReturn(seller);
        when(quoteRepository
                .findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc(
                        anyString())).thenReturn(Optional.empty());
        when(quoteRepository.save(quote)).thenReturn(quote);

        // WHEN
        Quote result = service.create(dto);

        // THEN
        assertSame(quote, result);
        assertSame(client, quote.getClient());
        assertSame(seller, quote.getSeller());

        assertEquals("Q-" + LocalDate.now().getYear() + "-000001", quote.getQuoteNumber());

        verify(quoteDomainService).initializeQuote(quote);
        verify(quoteDomainService).calculateTotals(quote);
        verify(quoteRepository).save(quote);
    }

    @Test
    void givenPreviousQuote_whenCreate_thenIncrementNumber() {

        // GIVEN
        QuoteDto dto = quoteDto();
        Quote quote = new Quote();

        prepareCreation(dto, quote);

        String prefix = "Q-" + LocalDate.now().getYear() + "-";

        Quote last = new Quote();
        last.setQuoteNumber(prefix + "000047");

        when(quoteRepository.findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc(
                        prefix)).thenReturn(Optional.of(last));

        // WHEN
        service.create(dto);

        // THEN
        assertEquals(prefix + "000048", quote.getQuoteNumber());
    }

    @Test
    void givenMissingClient_whenCreate_thenThrowAndNeverSave() {

        QuoteDto dto = quoteDto();

        when(clientRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> service.create(dto));

        verifyNoInteractions(quoteRepository, quoteDomainService);
    }

    // =====================================
    // INVENTORY ITEMS
    // =====================================

    @Test
    void givenValidInventoryItem_whenCreate_thenCopyPriceAndCalculate() {

        // GIVEN
        QuoteDto dto = quoteDto();

        QuoteItemDto itemDto = new QuoteItemDto();
        itemDto.setInventoryItemId(5L);
        itemDto.setQuantity(2);

        dto.setItems(List.of(itemDto));

        Quote quote = new Quote();

        prepareCreation(dto, quote);

        InventoryItem stockItem = new InventoryItem();

        stockItem.setId(5L);
        stockItem.setActive(true);
        stockItem.setName("Montura");
        stockItem.setSalePrice(new BigDecimal("80.00"));
        stockItem.setTaxRate(new BigDecimal("21.00"));

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(stockItem));

        // WHEN
        service.create(dto);

        // THEN
        assertEquals(1, quote.getItems().size());

        QuoteItem item = quote.getItems().getFirst();

        assertSame(quote, item.getQuote());
        assertSame(stockItem, item.getInventoryItem());

        assertEquals("Montura", item.getDescription());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("80.00"), item.getUnitPrice());
        assertEquals(BigDecimal.ZERO, item.getDiscount());

        verify(quoteDomainService).calculateItem(item);
        verify(quoteDomainService).calculateTotals(quote);
    }

    @Test
    void givenMissingInventoryItem_whenCreate_thenThrowAndNeverSave() {

        QuoteDto dto = quoteDto();

        QuoteItemDto item = new QuoteItemDto();
        item.setInventoryItemId(5L);

        dto.setItems(List.of(item));

        prepareCreation(dto, new Quote());

        when(inventoryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(InventoryItemNotFoundException.class, () -> service.create(dto));

        verify(quoteRepository, never()).save(any(Quote.class));
    }

    @Test
    void givenInactiveInventoryItem_whenCreate_thenThrow() {

        QuoteDto dto = quoteDto();

        QuoteItemDto item = new QuoteItemDto();
        item.setInventoryItemId(5L);

        dto.setItems(List.of(item));

        prepareCreation(dto, new Quote());

        InventoryItem inactive = new InventoryItem();

        inactive.setId(5L);
        inactive.setActive(false);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(inactive));

        assertThrows(InventoryItemNotFoundException.class, () -> service.create(dto));

        verify(quoteRepository, never()).save(any(Quote.class));
    }

    // =====================================
    // UPDATE
    // =====================================

    @Test
    void givenExistingQuote_whenUpdateWithoutItems_thenSaveAndRecalculate() {

        QuoteDto dto = new QuoteDto();
        Quote existing = new Quote();

        when(quoteRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(quoteRepository.save(existing)).thenReturn(existing);

        // WHEN
        Quote result = service.update(3L, dto);

        // THEN
        assertSame(existing, result);

        verify(quoteMapper).updateEntity(dto, existing);
        verify(quoteDomainService).calculateTotals(existing);
        verify(quoteRepository).save(existing);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void givenMissingQuote_whenUpdate_thenThrow() {

        when(quoteRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(QuoteNotFoundException.class, () -> service.update(3L, quoteDto()));

        verify(quoteRepository, never()).save(any(Quote.class));
    }

    // =====================================
    // DELETE
    // =====================================

    @Test
    void givenExistingQuote_whenDelete_thenDeactivateWithoutPhysicalDelete() {

        Quote quote = new Quote();

        when(quoteRepository.findById(3L)).thenReturn(Optional.of(quote));

        // WHEN
        service.delete(3L);

        // THEN
        assertFalse(quote.getActive());

        verify(quoteRepository).save(quote);
        verify(quoteRepository, never()).delete(any(Quote.class));
    }
}
