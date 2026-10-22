package com.optiflow.crm.inventory.specification;

import com.optiflow.crm.inventory.dto.InventorySearchDto;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;

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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventorySpecificationTest {

    private Root<InventoryItem> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    private InventorySearchDto searchDto;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);

        searchDto = new InventorySearchDto();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Predicate executeSpecification() {

        Specification<InventoryItem> specification = InventorySpecification.filterBy(searchDto);

        return specification.toPredicate(root, query, cb);
    }

    // =====================================
    // 1. SIN FILTROS
    // =====================================

    @Test
    void givenNoFilters_whenFilterBy_thenReturnNull() {

        // GIVEN
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
    void givenDefaultSearch_whenFilterBy_thenFilterActiveItems() {

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
    // 3. SKU
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenSku_whenFilterBy_thenUseLowercaseLike() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setSku("MON-001");

        Path<String> skuPath = mock(Path.class);

        Expression<String> lowerExpression = mock(Expression.class);

        when(root.<String>get("sku")).thenReturn(skuPath);
        when(cb.lower(skuPath)).thenReturn(lowerExpression);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lower(skuPath);
        verify(cb).like(lowerExpression, "%mon-001%");
    }

    // =====================================
    // 4. SKU VACÍO
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankSku_whenFilterBy_thenIgnoreFilter(String sku) {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setSku(sku);

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNull(result);
        verifyNoInteractions(root, cb);
    }

    // =====================================
    // 5. NAME
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenName_whenFilterBy_thenUseLowercaseLike() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setName("MONTURA");

        Path<String> namePath = mock(Path.class);

        Expression<String> lowerExpression = mock(Expression.class);

        when(root.<String>get("name")).thenReturn(namePath);
        when(cb.lower(namePath)).thenReturn(lowerExpression);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lower(namePath);
        verify(cb).like(lowerExpression, "%montura%");
    }

    // =====================================
    // 6. NAME VACÍO
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankName_whenFilterBy_thenIgnoreFilter(String name) {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setName(name);

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNull(result);
        verifyNoInteractions(root, cb);
    }

    // =====================================
    // 7. BRAND
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenBrand_whenFilterBy_thenUseLowercaseLike() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setBrand("RAY-BAN");

        Path<String> brandPath = mock(Path.class);

        Expression<String> lowerExpression = mock(Expression.class);

        when(root.<String>get("brand")).thenReturn(brandPath);
        when(cb.lower(brandPath)).thenReturn(lowerExpression);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lower(brandPath);
        verify(cb).like(lowerExpression, "%ray-ban%");
    }

    // =====================================
    // 8. BRAND VACÍO
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankBrand_whenFilterBy_thenIgnoreFilter(String brand) {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setBrand(brand);

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNull(result);
        verifyNoInteractions(root, cb);
    }

    // =====================================
    // 9. TYPE
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenType_whenFilterBy_thenUseEqual() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setType(InventoryItemType.FRAME);

        Path<InventoryItemType> typePath = mock(Path.class);

        when(root.<InventoryItemType>get("type")).thenReturn(typePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(typePath, InventoryItemType.FRAME);
    }

    // =====================================
    // 10. MIN PRICE
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenMinPrice_whenFilterBy_thenUseGreaterOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        BigDecimal price = new BigDecimal("50.00");

        searchDto.setMinPrice(price);

        Path<BigDecimal> pricePath = mock(Path.class);

        when(root.<BigDecimal>get("salePrice")).thenReturn(pricePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThanOrEqualTo(pricePath, price);
    }

    // =====================================
    // 11. MAX PRICE
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenMaxPrice_whenFilterBy_thenUseLessOrEqual() {

        // GIVEN
        searchDto.setActive(null);

        BigDecimal price = new BigDecimal("200.00");

        searchDto.setMaxPrice(price);

        Path<BigDecimal> pricePath = mock(Path.class);

        when(root.<BigDecimal>get("salePrice")).thenReturn(pricePath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).lessThanOrEqualTo(pricePath, price);
    }

    // =====================================
    // 12. IN STOCK TRUE
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenInStockTrue_whenFilterBy_thenStockGreaterThanZero() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setInStock(true);

        Path<Integer> stockPath = mock(Path.class);

        when(root.<Integer>get("stock")).thenReturn(stockPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).greaterThan(stockPath, 0);
    }

    // =====================================
    // 13. IN STOCK FALSE
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenInStockFalse_whenFilterBy_thenStockEqualsZero() {

        // GIVEN
        searchDto.setActive(null);
        searchDto.setInStock(false);

        Path<Integer> stockPath = mock(Path.class);

        when(root.<Integer>get("stock")).thenReturn(stockPath);

        // WHEN
        executeSpecification();

        // THEN
        verify(cb).equal(stockPath, 0);
    }

    // =====================================
    // 14. ACTIVE TRUE / FALSE
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
    // 15. FILTROS COMBINADOS
    // =====================================

    @Test
    @SuppressWarnings("unchecked")
    void givenMultipleFilters_whenFilterBy_thenApplyAllConditions() {

        // GIVEN
        searchDto.setType(InventoryItemType.FRAME);
        searchDto.setMinPrice(new BigDecimal("50.00"));
        searchDto.setInStock(true);
        searchDto.setActive(true);

        Path<InventoryItemType> typePath = mock(Path.class);
        Path<BigDecimal> pricePath = mock(Path.class);
        Path<Integer> stockPath = mock(Path.class);
        Path<Boolean> activePath = mock(Path.class);

        when(root.<InventoryItemType>get("type")).thenReturn(typePath);
        when(root.<BigDecimal>get("salePrice")).thenReturn(pricePath);
        when(root.<Integer>get("stock")).thenReturn(stockPath);
        when(root.<Boolean>get("active")).thenReturn(activePath);

        // Simulamos los predicados resultantes
        Predicate typePredicate = mock(Predicate.class);
        Predicate pricePredicate = mock(Predicate.class);
        Predicate stockPredicate = mock(Predicate.class);
        Predicate activePredicate = mock(Predicate.class);

        when(cb.equal(typePath, InventoryItemType.FRAME)).thenReturn(typePredicate);
        when(cb.greaterThanOrEqualTo(pricePath, new BigDecimal("50.00")))
                .thenReturn(pricePredicate);
        when(cb.greaterThan(stockPath, 0)).thenReturn(stockPredicate);
        when(cb.equal(activePath, true)).thenReturn(activePredicate);

        // Permitimos combinar predicados mediante AND
        when(cb.and(any(Predicate.class), any(Predicate.class)))
                .thenReturn(mock(Predicate.class));

        // WHEN
        Predicate result = executeSpecification();

        // THEN
        assertNotNull(result);

        verify(cb).equal(typePath, InventoryItemType.FRAME);
        verify(cb).greaterThanOrEqualTo(pricePath, new BigDecimal("50.00"));
        verify(cb).greaterThan(stockPath, 0);
        verify(cb).equal(activePath, true);

        // Verificamos que los filtros se combinan
        verify(cb, times(3))
                .and(any(Predicate.class), any(Predicate.class));
    }
}
