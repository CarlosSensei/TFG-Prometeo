package com.optiflow.crm.order.specification;

import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderStatus;

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

class OrderSpecificationTest {

    private Root<Order> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    private OrderSearchDto searchDto;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {

        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);

        searchDto = new OrderSearchDto();
    }

    // =====================================
    // MÉTODO AUXILIAR
    // =====================================

    private Predicate executeSpecification() {

        Specification<Order> specification =
                OrderSpecification.filterBy(searchDto);

        return specification.toPredicate(root, query, cb);
    }

    // =====================================
    // 1. SIN FILTROS
    // =====================================

    @Test
    void givenNoFilters_whenFilterBy_thenReturnNull() {

        // GIVEN
        // El DTO tiene active=true por defecto.
        // Lo anulamos para no aplicar ningún filtro.
        searchDto.setActive(null);

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNull(result);

        verifyNoInteractions(root, cb);
    }

    // =====================================
    // 2. FILTRO ACTIVE POR DEFECTO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenDefaultSearch_whenFilterBy_thenFilterActiveOrders() {

        // GIVEN
        Path<Boolean> activePath = mock(Path.class);

        when(root.<Boolean>get("active"))
                .thenReturn(activePath);

        // WHEN
        executeSpecification();

        // THEN
        assertEquals(true, searchDto.getActive());

        verify(cb).equal(activePath, true);
    }

    // =====================================
    // 3. ORDER NUMBER
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenOrderNumber_whenFilterBy_thenUseLowercaseLike() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setOrderNumber("O-2026");

        Path<String> numberPath = mock(Path.class);
        Expression<String> lowerExpression =
                mock(Expression.class);

        when(root.<String>get("orderNumber"))
                .thenReturn(numberPath);

        when(cb.lower(numberPath))
                .thenReturn(lowerExpression);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lower(numberPath);

        verify(cb).like(
                lowerExpression,
                "%o-2026%"
        );
    }

    // =====================================
    // 4. ORDER NUMBER VACÍO
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankOrderNumber_whenFilterBy_thenIgnoreIt(
            String orderNumber) {

        // GIVEN
        searchDto.setOrderNumber(orderNumber);
        searchDto.setActive(null);

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNull(result);

        verifyNoInteractions(root, cb);
    }

    // =====================================
    // 5. CLIENT ID
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenClientId_whenFilterBy_thenUseClientRelation() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setClientId(7L);

        Path<Object> clientPath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);

        when(root.get("client"))
                .thenReturn(clientPath);

        when(clientPath.<Long>get("id"))
                .thenReturn(idPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(idPath, 7L);
    }

    // =====================================
    // 6. QUOTE ID
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenQuoteId_whenFilterBy_thenUseQuoteRelation() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setQuoteId(5L);

        Path<Object> quotePath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);

        when(root.get("quote"))
                .thenReturn(quotePath);

        when(quotePath.<Long>get("id"))
                .thenReturn(idPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(idPath, 5L);
    }

    // =====================================
    // 7. STATUS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenStatus_whenFilterBy_thenUseEqual() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setStatus(OrderStatus.PENDING);

        Path<OrderStatus> statusPath = mock(Path.class);

        when(root.<OrderStatus>get("status"))
                .thenReturn(statusPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(
                statusPath,
                OrderStatus.PENDING
        );
    }

    // =====================================
    // 8. ORDER DATE FROM
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenOrderDateFrom_whenFilterBy_thenUseGreaterOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDate date = LocalDate.of(2026, 10, 1);
        searchDto.setOrderDateFrom(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("orderDate"))
                .thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 9. ORDER DATE TO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenOrderDateTo_whenFilterBy_thenUseLessOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDate date = LocalDate.of(2026, 10, 31);
        searchDto.setOrderDateTo(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("orderDate"))
                .thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lessThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 10. EXPECTED DELIVERY DATE FROM
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenExpectedDeliveryFrom_whenFilterBy_thenUseGreaterOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDate date = LocalDate.of(2026, 11, 1);

        searchDto.setExpectedDeliveryDateFrom(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("expectedDeliveryDate"))
                .thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 11. EXPECTED DELIVERY DATE TO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenExpectedDeliveryTo_whenFilterBy_thenUseLessOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDate date = LocalDate.of(2026, 11, 30);

        searchDto.setExpectedDeliveryDateTo(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("expectedDeliveryDate"))
                .thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lessThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 12. DELIVERY DATE FROM
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenDeliveryDateFrom_whenFilterBy_thenUseGreaterOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDate date = LocalDate.of(2026, 11, 10);
        searchDto.setDeliveryDateFrom(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("deliveryDate"))
                .thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 13. DELIVERY DATE TO
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenDeliveryDateTo_whenFilterBy_thenUseLessOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        LocalDate date = LocalDate.of(2026, 11, 25);
        searchDto.setDeliveryDateTo(date);

        Path<LocalDate> datePath = mock(Path.class);

        when(root.<LocalDate>get("deliveryDate"))
                .thenReturn(datePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lessThanOrEqualTo(datePath, date);
    }

    // =====================================
    // 14. ACTIVE TRUE / FALSE
    // =====================================

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("unchecked")
    void givenActive_whenFilterBy_thenUseEqual(
            boolean active) {

        // GIVEN
        searchDto.setActive(active);

        Path<Boolean> activePath = mock(Path.class);

        when(root.<Boolean>get("active"))
                .thenReturn(activePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(activePath, active);
    }

    // =====================================
    // 15. COMBINACIÓN DE FILTROS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenMultipleFilters_whenFilterBy_thenCombineWithAnd() {

        // GIVEN
        searchDto.setClientId(7L);
        searchDto.setStatus(OrderStatus.PENDING);
        searchDto.setActive(true);

        Path<Object> clientPath = mock(Path.class);
        Path<Long> clientIdPath = mock(Path.class);
        Path<OrderStatus> statusPath = mock(Path.class);
        Path<Boolean> activePath = mock(Path.class);

        Predicate clientPredicate = mock(Predicate.class);
        Predicate statusPredicate = mock(Predicate.class);
        Predicate activePredicate = mock(Predicate.class);

        when(root.get("client")).thenReturn(clientPath);

        when(clientPath.<Long>get("id"))
                .thenReturn(clientIdPath);

        when(root.<OrderStatus>get("status"))
                .thenReturn(statusPath);

        when(root.<Boolean>get("active"))
                .thenReturn(activePath);

        when(cb.equal(clientIdPath, 7L))
                .thenReturn(clientPredicate);

        when(cb.equal(statusPath, OrderStatus.PENDING))
                .thenReturn(statusPredicate);

        when(cb.equal(activePath, true))
                .thenReturn(activePredicate);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(clientIdPath, 7L);
        verify(cb).equal(statusPath, OrderStatus.PENDING);
        verify(cb).equal(activePath, true);

        // Specification.allOf combina mediante AND.
        verify(cb, atLeastOnce()).and(
                any(Predicate.class),
                any(Predicate.class)
        );
    }
}
