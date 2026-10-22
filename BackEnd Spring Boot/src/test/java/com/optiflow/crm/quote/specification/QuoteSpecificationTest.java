package com.optiflow.crm.quote.specification;

import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuoteSpecificationTest {

    private Root<Quote> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    private QuoteSearchDto searchDto;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {

        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);

        searchDto = new QuoteSearchDto();
    }

    // =====================================
    // MÉTODO AUXILIAR
    // =====================================

    private Predicate executeSpecification() {

        Specification<Quote> spec = QuoteSpecification.filterBy(searchDto);

        return spec.toPredicate(root, query, cb);
    }

    // =====================================
    // SIN FILTROS
    // =====================================

    @Test
    void givenNoFilters_whenFilterBy_thenReturnNoRestrictions() {

        // GIVEN
        searchDto.setActive(null);

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNull(result);

        verifyNoInteractions(root, cb);
    }

    // =====================================
    // QUOTE NUMBER
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenQuoteNumber_whenFilterBy_thenUseLowercaseLike() {

        // GIVEN
        searchDto.setQuoteNumber("Q-2026");

        Path<String> numberPath = mock(Path.class);
        Expression<String> lowerExpression = mock(Expression.class);

        when(root.<String>get("quoteNumber")).thenReturn(numberPath);
        when(cb.lower(numberPath)).thenReturn(lowerExpression);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lower(numberPath);
        verify(cb).like(lowerExpression, "%q-2026%");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankQuoteNumber_whenFilterBy_thenIgnoreIt(
            String quoteNumber) {

        // GIVEN
        searchDto.setQuoteNumber(quoteNumber);

        // Desactivamos el filtro active por defecto
        searchDto.setActive(null);

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNull(result);

        verifyNoInteractions(root, cb);
    }

    // =====================================
    // CLIENT ID
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenClientId_whenFilterBy_thenUseClientRelation() {

        // GIVEN
        searchDto.setClientId(7L);

        Path<Client> clientPath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);

        when(root.<Client>get("client")).thenReturn(clientPath);

        when(clientPath.<Long>get("id")).thenReturn(idPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(idPath, 7L);
    }

    // =====================================
    // STATUS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenStatus_whenFilterBy_thenUseEqual() {

        // GIVEN
        searchDto.setStatus(QuoteStatus.DRAFT);

        Path<QuoteStatus> statusPath = mock(Path.class);

        when(root.<QuoteStatus>get("status")).thenReturn(statusPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(statusPath, QuoteStatus.DRAFT);
    }

    // =====================================
    // QUOTE DATE FROM
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenQuoteDateFrom_whenFilterBy_thenUseGreaterOrEqual() {

        // GIVEN
        LocalDate date = LocalDate.of(2026, 10, 1);

        searchDto.setQuoteDateFrom(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("quoteDate")).thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThanOrEqualTo(datePath, date);
    }

    // =====================================
    // QUOTE DATE TO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenQuoteDateTo_whenFilterBy_thenUseLessOrEqual() {

        // GIVEN
        LocalDate date = LocalDate.of(2026, 10, 31);

        searchDto.setQuoteDateTo(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("quoteDate")).thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lessThanOrEqualTo(datePath, date);
    }

    // =====================================
    // EXPIRATION DATE FROM
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenExpirationFrom_whenFilterBy_thenUseGreaterOrEqual() {

        // GIVEN
        LocalDate date = LocalDate.of(2026, 11, 1);

        searchDto.setExpirationDateFrom(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("expirationDate")).thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThanOrEqualTo(datePath, date);
    }

    // =====================================
    // EXPIRATION DATE TO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenExpirationTo_whenFilterBy_thenUseLessOrEqual() {

        // GIVEN
        LocalDate date = LocalDate.of(2026, 11, 30);

        searchDto.setExpirationDateTo(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("expirationDate")).thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lessThanOrEqualTo(datePath, date);
    }

    // =====================================
    // ACTIVE
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
    // COMBINACIÓN DE FILTROS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenMultipleFilters_whenFilterBy_thenCombineWithAnd() {

        // GIVEN
        searchDto.setClientId(7L);
        searchDto.setStatus(QuoteStatus.DRAFT);
        searchDto.setActive(true);

        Path<Client> clientPath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);
        Path<QuoteStatus> statusPath = mock(Path.class);
        Path<Boolean> activePath = mock(Path.class);

        Predicate clientPredicate = mock(Predicate.class);
        Predicate statusPredicate = mock(Predicate.class);
        Predicate activePredicate = mock(Predicate.class);

        when(root.<Client>get("client")).thenReturn(clientPath);
        when(clientPath.<Long>get("id")).thenReturn(idPath);
        when(root.<QuoteStatus>get("status")).thenReturn(statusPath);
        when(root.<Boolean>get("active")).thenReturn(activePath);
        when(cb.equal(idPath, 7L)).thenReturn(clientPredicate);
        when(cb.equal(statusPath, QuoteStatus.DRAFT)).thenReturn(statusPredicate);
        when(cb.equal(activePath, true)).thenReturn(activePredicate);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(idPath, 7L);
        verify(cb).equal(statusPath, QuoteStatus.DRAFT);
        verify(cb).equal(activePath, true);

        // Comprobamos que CriteriaBuilder
        // combina las condiciones.
        verify(cb, atLeastOnce()).and(any(Predicate.class), any(Predicate.class));
    }
}
