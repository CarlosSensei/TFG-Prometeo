package com.optiflow.crm.followup;

import com.optiflow.crm.followup.dto.CompleteFollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.dto.CreateFollowUpDto;
import com.optiflow.crm.followup.mapper.FollowUpMapper;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.shared.exception.FollowUpNotFoundException;

import com.optiflow.crm.shared.exception.QuoteNotFoundException;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUpServiceImplTest {

    @Mock
    private FollowUpRepository followUpRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private FollowUpMapper followUpMapper;

    @InjectMocks
    private FollowUpServiceImpl service;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private FollowUp createFollowUp(Long id) {

        FollowUp followUp = new FollowUp();

        followUp.setId(id);
        followUp.setStatus(FollowUpStatus.PENDING);
        followUp.setActive(true);
        followUp.setAutomated(false);
        followUp.setScheduledAt(LocalDateTime
                .of(2026, 10, 25, 10, 0));
        followUp.setNotes("Llamar al cliente");

        return followUp;
    }

    private FollowUpDto createDto() {

        FollowUpDto dto = new FollowUpDto();

        dto.setQuoteId(5L);
        dto.setScheduledAt(LocalDateTime
                .of(2026, 10, 25, 10, 0));

        return dto;
    }

    private CreateFollowUpDto createFollowUpCreateDto() {

        CreateFollowUpDto dto = new CreateFollowUpDto();

        dto.setType(FollowUpType.CALL);
        dto.setScheduledAt(LocalDateTime
                .of(2026, 11, 10, 10, 0));
        dto.setNotes("Llamar al cliente");
        dto.setAutomated(null);

        return dto;
    }

    // =====================================
    // 1. FIND ALL
    // =====================================

    @Test
    void givenActiveFollowUps_whenFindAll_thenReturnPage() {

        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);

        FollowUp followUp = createFollowUp(1L);

        Page<FollowUp> expected = new PageImpl<>(List.of(followUp), pageable, 1);

        when(followUpRepository.findByActiveTrue(pageable)).thenReturn(expected);

        // WHEN
        Page<FollowUp> result = service.findAll(pageable);

        // THEN
        assertSame(expected, result);
        assertEquals(1, result.getTotalElements());

        verify(followUpRepository).findByActiveTrue(pageable);
    }

    // =====================================
    // 2. SEARCH CON PAGINACIÓN
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenSearchCriteria_whenSearch_thenApplyPaginationAndSort() {

        // GIVEN
        FollowUpSearchDto dto = new FollowUpSearchDto();

        dto.setPage(2);
        dto.setSize(5);
        dto.setSortBy("scheduledAt");
        dto.setSortDirection("desc");

        when(followUpRepository.findAll(any(Specification.class),
                any(Pageable.class))).thenReturn(Page.empty());

        // WHEN
        service.search(dto);

        // THEN
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(followUpRepository).findAll(any(Specification.class), captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());

        assertEquals(Sort.Direction.DESC, pageable.getSort()
                .getOrderFor("scheduledAt").getDirection());
    }

    // =====================================
    // 3. SEARCH CON VALORES POR DEFECTO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenNullPagination_whenSearch_thenUseDefaults() {

        // GIVEN
        FollowUpSearchDto dto = new FollowUpSearchDto();

        dto.setPage(null);
        dto.setSize(null);
        dto.setSortBy(null);
        dto.setSortDirection(null);

        when(followUpRepository.findAll(any(Specification.class),
                any(Pageable.class))).thenReturn(Page.empty());

        // WHEN
        service.search(dto);

        // THEN
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(followUpRepository).findAll(any(Specification.class), captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(Sort.Direction.ASC, pageable.getSort()
                .getOrderFor("scheduledAt").getDirection());
    }

    // =====================================
    // 4. FIND BY QUOTE ID
    // =====================================

    @Test
    void givenQuoteId_whenFindByQuoteId_thenReturnPage() {

        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);

        Page<FollowUp> expected = new PageImpl<>(List
                .of(createFollowUp(1L)), pageable, 1);

        when(followUpRepository.findByQuoteIdAndActiveTrue(5L, pageable))
                .thenReturn(expected);

        // WHEN
        Page<FollowUp> result = service.findByQuoteId(5L, pageable);

        // THEN
        assertSame(expected, result);

        verify(followUpRepository).findByQuoteIdAndActiveTrue(5L, pageable);
    }

    // =====================================
    // 5. FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturnFollowUp() {

        // GIVEN
        FollowUp followUp = createFollowUp(1L);

        when(followUpRepository.findById(1L)).thenReturn(Optional.of(followUp));

        // WHEN
        FollowUp result = service.findById(1L);

        // THEN
        assertSame(followUp, result);
    }

    @Test
    void givenUnknownId_whenFindById_thenThrowNotFound() {

        // GIVEN
        when(followUpRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(FollowUpNotFoundException.class, () -> service.findById(99L));
    }

    // =====================================
    // 6. CREATE CON VALORES POR DEFECTO
    // =====================================


    @Test
    void givenValidDto_whenCreate_thenSetDefaultsAndSave() {

        // GIVEN
        CreateFollowUpDto dto = createFollowUpCreateDto();

        Quote quote = new Quote();
        quote.setId(5L);

        when(quoteRepository.findById(5L)).thenReturn(Optional.of(quote));
        when(followUpRepository.save(any(FollowUp.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        FollowUp result = service.create(5L, dto);

        // THEN
        assertNotNull(result);

        assertAll(
                () -> assertSame(quote, result.getQuote()),
                () -> assertEquals(FollowUpType.CALL, result.getType()),
                () -> assertEquals(FollowUpStatus.PENDING, result.getStatus()),
                () -> assertEquals(dto.getScheduledAt(), result.getScheduledAt()),
                () -> assertEquals(dto.getNotes(), result.getNotes()),
                () -> assertFalse(result.getAutomated()),
                () -> assertTrue(result.getActive()));

        verify(quoteRepository).findById(5L);
        verify(followUpRepository).save(result);

        // La creación ya no utiliza el mapper.
        verifyNoInteractions(followUpMapper);
    }

    // =====================================
    // 7. CREATE CONSERVANDO VALORES
    // =====================================


    @Test
    void givenAutomatedTrue_whenCreate_thenPreserveAutomated() {

        // GIVEN
        CreateFollowUpDto dto = createFollowUpCreateDto();
        dto.setAutomated(true);

        Quote quote = new Quote();
        quote.setId(5L);

        when(quoteRepository.findById(5L)).thenReturn(Optional.of(quote));
        when(followUpRepository.save(any(FollowUp.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        FollowUp result = service.create(5L, dto);

        // THEN
        assertAll(
                () -> assertSame(quote, result.getQuote()),
                () -> assertEquals(FollowUpStatus.PENDING, result.getStatus()),
                () -> assertTrue(result.getAutomated()),
                () -> assertTrue(result.getActive()),
                () -> assertEquals(FollowUpType.CALL, result.getType()));

        verify(quoteRepository).findById(5L);
        verify(followUpRepository).save(result);

        verifyNoInteractions(followUpMapper);
    }


    // =====================================
    // 8. CREATE CON PRESUPUESTO INEXISTENTE
    // =====================================


    @Test
    void givenUnknownQuote_whenCreate_thenThrowWithoutSaving() {

        // GIVEN
        CreateFollowUpDto dto = createFollowUpCreateDto();

        when(quoteRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class, () -> service.create(99L, dto));

        assertEquals("Presupuesto no encontrado con id: 99",
                exception.getMessage());

        verify(quoteRepository).findById(99L);
        // No debe crear entidades si el presupuesto no existe.
        verify(followUpRepository, never()).save(any(FollowUp.class));
        verifyNoInteractions(followUpMapper);
    }


    // =====================================
    // 9. UPDATE
    // =====================================

    @Test
    void givenExistingFollowUp_whenUpdate_thenSaveModifiedEntity() {

        // GIVEN
        FollowUp existing = createFollowUp(7L);
        FollowUpDto dto = createDto();

        when(followUpRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(followUpRepository.save(existing)).thenReturn(existing);

        // WHEN
        FollowUp result = service.update(7L, dto);

        // THEN
        assertSame(existing, result);

        verify(followUpMapper).updateEntity(dto, existing);
        verify(followUpRepository).save(existing);
    }

    // =====================================
    // 10. UPDATE INEXISTENTE
    // =====================================

    @Test
    void givenUnknownFollowUp_whenUpdate_thenThrow() {

        // GIVEN
        when(followUpRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(FollowUpNotFoundException.class,
                () -> service.update(99L, createDto()));

        verifyNoInteractions(followUpMapper);
        verify(followUpRepository, never()).save(any(FollowUp.class));
    }

    // =====================================
    // 11. COMPLETE
    // =====================================

    @Test
    void givenPendingFollowUp_whenComplete_thenSetCompletedFields() {

        // GIVEN
        FollowUp followUp = createFollowUp(8L);

        CompleteFollowUpDto dto = new CompleteFollowUpDto();

        dto.setResult("Cliente interesado");
        dto.setNotes("Volver a llamar la próxima semana");

        when(followUpRepository.findById(8L)).thenReturn(Optional.of(followUp));
        when(followUpRepository.save(followUp)).thenReturn(followUp);

        LocalDateTime before = LocalDateTime.now();

        // WHEN
        FollowUp result = service.complete(8L, dto);

        LocalDateTime after = LocalDateTime.now();

        // THEN
        assertSame(followUp, result);
        assertEquals(FollowUpStatus.COMPLETED, result.getStatus());
        assertEquals("Cliente interesado", result.getResult());
        assertEquals("Volver a llamar la próxima semana", result.getNotes());
        assertNotNull(result.getCompletedAt());
        assertFalse(result.getCompletedAt().isBefore(before));
        assertFalse(result.getCompletedAt().isAfter(after));

        verify(followUpRepository).save(followUp);
    }

    // =====================================
    // 12. COMPLETE SIN NOTAS
    // =====================================

    @Test
    void givenNullNotes_whenComplete_thenPreserveExistingNotes() {

        // GIVEN
        FollowUp followUp = createFollowUp(8L);

        CompleteFollowUpDto dto = new CompleteFollowUpDto();

        dto.setResult("Sin respuesta");
        dto.setNotes(null);

        when(followUpRepository.findById(8L)).thenReturn(Optional.of(followUp));
        when(followUpRepository.save(followUp)).thenReturn(followUp);

        // WHEN
        FollowUp result = service.complete(8L, dto);

        // THEN
        assertEquals("Llamar al cliente", result.getNotes());
        assertEquals("Sin respuesta", result.getResult());
        assertEquals(FollowUpStatus.COMPLETED, result.getStatus());
    }

    // =====================================
    // 13. COMPLETE INEXISTENTE
    // =====================================

    @Test
    void givenUnknownFollowUp_whenComplete_thenThrow() {

        // GIVEN
        when(followUpRepository.findById(99L)).thenReturn(Optional.empty());

        CompleteFollowUpDto dto = new CompleteFollowUpDto();
        dto.setResult("Completado");

        // WHEN + THEN
        assertThrows(FollowUpNotFoundException.class, () -> service.complete(99L, dto));
        verify(followUpRepository, never()).save(any(FollowUp.class));
    }

    // =====================================
    // 14. DELETE LÓGICO
    // =====================================

    @Test
    void givenExistingFollowUp_whenDelete_thenDeactivate() {

        // GIVEN
        FollowUp followUp = createFollowUp(5L);

        when(followUpRepository.findById(5L)).thenReturn(Optional.of(followUp));

        // WHEN
        service.delete(5L);

        // THEN
        assertFalse(followUp.getActive());

        verify(followUpRepository).save(followUp);
        verify(followUpRepository, never()).delete(any(FollowUp.class));
        verify(followUpRepository, never()).deleteById(anyLong());
    }

    // =====================================
    // 15. DELETE INEXISTENTE
    // =====================================

    @Test
    void givenUnknownFollowUp_whenDelete_thenThrow() {

        // GIVEN
        when(followUpRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(FollowUpNotFoundException.class, () -> service.delete(99L));

        verify(followUpRepository, never()).save(any(FollowUp.class));
    }

    // =====================================
    // 16. DELETE YA INACTIVO
    // =====================================

    @Test
    void givenInactiveFollowUp_whenDelete_thenRemainInactive() {

        // GIVEN
        FollowUp followUp = createFollowUp(5L);
        followUp.setActive(false);

        when(followUpRepository.findById(5L)).thenReturn(Optional.of(followUp));

        // WHEN
        service.delete(5L);

        // THEN
        assertFalse(followUp.getActive());

        verify(followUpRepository).save(followUp);
    }
}
