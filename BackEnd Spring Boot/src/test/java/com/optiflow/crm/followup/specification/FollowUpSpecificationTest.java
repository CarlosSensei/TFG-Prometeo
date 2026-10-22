package com.optiflow.crm.followup.specification;

import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FollowUpSpecificationTest {

    private Root<FollowUp> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    private FollowUpSearchDto searchDto;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);

        searchDto = new FollowUpSearchDto();
    }

    // =====================================
    // MÉTODO AUXILIAR
    // =====================================

    private Predicate executeSpecification() {

        Specification<FollowUp> specification = FollowUpSpecification.filterBy(searchDto);

        return specification.toPredicate(root, query, cb);
    }

    // =====================================
    // 1. SIN FILTROS
    // =====================================

    @Test
    void givenNoFilters_whenFilterBy_thenReturnNull() {

        // GIVEN
        // El DTO inicializa active=true.
        searchDto.setActive(null);

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNull(result);

        verifyNoInteractions(root, cb);
    }

    // =====================================
    // 2. ACTIVE POR DEFECTO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenDefaultSearch_whenFilterBy_thenFilterActive() {

        // GIVEN
        Path<Boolean> activePath = mock(Path.class);

        when(root.<Boolean>get("active")).thenReturn(activePath);

        // WHEN
        executeSpecification();

        // THEN
        assertEquals(true, searchDto.getActive());

        verify(cb).equal(activePath, true);
    }

    // =====================================
    // 3. QUOTE ID
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenQuoteId_whenFilterBy_thenUseQuoteRelation() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setQuoteId(5L);

        Path<Object> quotePath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);

        when(root.get("quote")).thenReturn(quotePath);
        when(quotePath.<Long>get("id")).thenReturn(idPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(root).get("quote");
        verify(quotePath).get("id");
        verify(cb).equal(idPath, 5L);
    }

    // =====================================
    // 4. TYPE
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenType_whenFilterBy_thenUseEqual() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setType(FollowUpType.CALL);

        Path<FollowUpType> typePath = mock(Path.class);

        when(root.<FollowUpType>get("type")).thenReturn(typePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(typePath, FollowUpType.CALL);
    }

    // =====================================
    // 5. STATUS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenStatus_whenFilterBy_thenUseEqual() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setStatus(FollowUpStatus.PENDING);

        Path<FollowUpStatus> statusPath = mock(Path.class);

        when(root.<FollowUpStatus>get("status")).thenReturn(statusPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(statusPath, FollowUpStatus.PENDING);
    }

    // =====================================
    // 6. SCHEDULED FROM
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenScheduledFrom_whenFilterBy_thenGreaterOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDateTime date = LocalDateTime
                .of(2026, 10, 1, 9, 0);

        searchDto.setScheduledFrom(date);

        Path<LocalDateTime> datePath = mock(Path.class);

        when(root.<LocalDateTime>get("scheduledAt")).thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 7. SCHEDULED TO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenScheduledTo_whenFilterBy_thenLessOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDateTime date = LocalDateTime
                .of(2026, 10, 31, 23, 59);

        searchDto.setScheduledTo(date);

        Path<LocalDateTime> datePath = mock(Path.class);

        when(root.<LocalDateTime>get("scheduledAt")).thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lessThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 8. COMPLETED FROM
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenCompletedFrom_whenFilterBy_thenGreaterOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDateTime date = LocalDateTime
                .of(2026, 10, 10, 8, 0);

        searchDto.setCompletedFrom(date);

        Path<LocalDateTime> datePath = mock(Path.class);

        when(root.<LocalDateTime>get("completedAt")).thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 9. COMPLETED TO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenCompletedTo_whenFilterBy_thenLessOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDateTime date = LocalDateTime
                .of(2026, 10, 20, 18, 0);

        searchDto.setCompletedTo(date);

        Path<LocalDateTime> datePath = mock(Path.class);

        when(root.<LocalDateTime>get("completedAt")).thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lessThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 10. AUTOMATED TRUE / FALSE
    // =====================================

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("unchecked")
    void givenAutomated_whenFilterBy_thenUseEqual(boolean automated) {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setAutomated(automated);

        Path<Boolean> automatedPath = mock(Path.class);

        when(root.<Boolean>get("automated")).thenReturn(automatedPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(automatedPath, automated);
    }

    // =====================================
    // 11. ACTIVE TRUE / FALSE
    // =====================================

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("unchecked")
    void givenActive_whenFilterBy_thenUseEqual(boolean active) {

        // GIVEN
        searchDto.setActive(active);

        Path<Boolean> activePath = mock(Path.class);

        when(root.<Boolean>get("active")).thenReturn(activePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(activePath, active);
    }

    // =====================================
    // 12. FILTROS COMBINADOS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenMultipleFilters_whenFilterBy_thenCombineWithAnd() {

        // GIVEN
        searchDto.setQuoteId(5L);
        searchDto.setStatus(FollowUpStatus.PENDING);
        searchDto.setAutomated(false);
        searchDto.setActive(true);

        Path<Object> quotePath = mock(Path.class);
        Path<Long> quoteIdPath = mock(Path.class);
        Path<FollowUpStatus> statusPath = mock(Path.class);
        Path<Boolean> automatedPath = mock(Path.class);
        Path<Boolean> activePath = mock(Path.class);

        when(root.get("quote")).thenReturn(quotePath);
        when(quotePath.<Long>get("id")).thenReturn(quoteIdPath);
        when(root.<FollowUpStatus>get("status")).thenReturn(statusPath);
        when(root.<Boolean>get("automated")).thenReturn(automatedPath);
        when(root.<Boolean>get("active")).thenReturn(activePath);

        // Cada condición debe producir un Predicate,
        // no null, para que Specification.allOf()
        // pueda combinar correctamente las condiciones.

        Predicate quotePredicate = mock(Predicate.class);
        Predicate statusPredicate = mock(Predicate.class);
        Predicate automatedPredicate = mock(Predicate.class);
        Predicate activePredicate = mock(Predicate.class);

        when(cb.equal(quoteIdPath, 5L)).thenReturn(quotePredicate);
        when(cb.equal(statusPath, FollowUpStatus.PENDING)).thenReturn(statusPredicate);
        when(cb.equal(automatedPath, false)).thenReturn(automatedPredicate);
        when(cb.equal(activePath, true)).thenReturn(activePredicate);
        when(cb.and(any(Predicate.class), any(Predicate.class)))
                .thenReturn(mock(Predicate.class));

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNotNull(result);

        verify(cb).equal(quoteIdPath, 5L);
        verify(cb).equal(statusPath, FollowUpStatus.PENDING);
        verify(cb).equal(automatedPath, false);
        verify(cb).equal(activePath, true);

        // Cuatro predicados se combinan
        // mediante tres operaciones AND.
        verify(cb, times(3)).and(any(Predicate.class),
                any(Predicate.class));
    }

    // =====================================
    // 13. RANGO DE FECHAS PROGRAMADAS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenScheduledDateRange_whenFilterBy_thenApplyBothBounds() {

        // GIVEN
        searchDto.setActive(null);

        LocalDateTime from = LocalDateTime
                .of(2026, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime
                .of(2026, 10, 31, 23, 59);

        searchDto.setScheduledFrom(from);
        searchDto.setScheduledTo(to);

        Path<LocalDateTime> datePath = mock(Path.class);

        when(root.<LocalDateTime>get("scheduledAt")).thenReturn(datePath);

        Predicate fromPredicate = mock(Predicate.class);
        Predicate toPredicate = mock(Predicate.class);

        when(cb.greaterThanOrEqualTo(datePath, from)).thenReturn(fromPredicate);
        when(cb.lessThanOrEqualTo(datePath, to)).thenReturn(toPredicate);
        when(cb.and(fromPredicate, toPredicate)).thenReturn(mock(Predicate.class));

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNotNull(result);

        verify(cb).greaterThanOrEqualTo(datePath, from);
        verify(cb).lessThanOrEqualTo(datePath, to);
        verify(cb).and(fromPredicate, toPredicate);
    }

    // =====================================
    // 14. RANGO DE FECHAS COMPLETADAS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenCompletedDateRange_whenFilterBy_thenApplyBothBounds() {

        // GIVEN
        searchDto.setActive(null);

        LocalDateTime from = LocalDateTime
                .of(2026, 10, 5, 0, 0);
        LocalDateTime to = LocalDateTime
                .of(2026, 10, 20, 23, 59);

        searchDto.setCompletedFrom(from);
        searchDto.setCompletedTo(to);

        Path<LocalDateTime> datePath = mock(Path.class);

        when(root.<LocalDateTime>get("completedAt")).thenReturn(datePath);

        Predicate fromPredicate = mock(Predicate.class);
        Predicate toPredicate = mock(Predicate.class);

        when(cb.greaterThanOrEqualTo(datePath, from)).thenReturn(fromPredicate);
        when(cb.lessThanOrEqualTo(datePath, to)).thenReturn(toPredicate);
        when(cb.and(fromPredicate, toPredicate)).thenReturn(mock(Predicate.class));

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNotNull(result);

        verify(cb).greaterThanOrEqualTo(datePath, from);
        verify(cb).lessThanOrEqualTo(datePath, to);
        verify(cb).and(fromPredicate, toPredicate);
    }
}
