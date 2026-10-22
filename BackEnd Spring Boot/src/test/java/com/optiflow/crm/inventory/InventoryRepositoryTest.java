package com.optiflow.crm.inventory;

import com.optiflow.crm.inventory.dto.InventorySearchDto;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;
import com.optiflow.crm.inventory.specification.InventorySpecification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository repository;

    @Autowired
    private EntityManager entityManager;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private InventoryItem createItem(
            String sku,
            String name,
            InventoryItemType type,
            String brand,
            String price,
            int stock,
            boolean active) {

        InventoryItem item = new InventoryItem();

        item.setSku(sku);
        item.setName(name);
        item.setType(type);
        item.setBrand(brand);
        item.setSalePrice(new BigDecimal(price));
        item.setTaxRate(new BigDecimal("21.00"));
        item.setStock(stock);
        item.setActive(active);

        return repository.saveAndFlush(item);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private InventorySearchDto searchDto() {
        return new InventorySearchDto();
    }

    private Page<InventoryItem> executeSearch(InventorySearchDto search) {

        return repository.findAll(InventorySpecification.filterBy(search),
                PageRequest.of(0, 10, Sort.by("sku")
                        .ascending()));
    }

    // =====================================
    // 1. SAVE AND FIND BY ID
    // =====================================

    @Test
    void givenValidItem_whenSave_thenPersistAllFields() {

        // GIVEN
        InventoryItem item = createItem(
                "MON-001",
                "Montura graduada",
                InventoryItemType.FRAME,
                "Ray-Ban",
                "120.00",
                10,
                true
        );

        Long id = item.getId();

        flushAndClear();

        // WHEN
        Optional<InventoryItem> result = repository.findById(id);

        // THEN
        assertTrue(result.isPresent());

        InventoryItem found = result.get();

        assertAll(
                () -> assertEquals(id, found.getId()),
                () -> assertEquals("MON-001", found.getSku()),
                () -> assertEquals("Montura graduada", found.getName()),
                () -> assertEquals(InventoryItemType.FRAME, found.getType()),
                () -> assertEquals("Ray-Ban", found.getBrand()),
                () -> assertEquals(0, new BigDecimal("120.00")
                        .compareTo(found.getSalePrice())),
                () -> assertEquals(10, found.getStock()),
                () -> assertTrue(found.getActive()),
                () -> assertNotNull(found.getCreatedAt()),
                () -> assertNotNull(found.getUpdatedAt())
        );
    }

    // =====================================
    // 2. FIND BY SKU IGNORE CASE
    // =====================================

    @Test
    void givenExistingSku_whenFindIgnoringCase_thenReturnItem() {

        // GIVEN
        createItem(
                "MON-002", "Montura",
                InventoryItemType.FRAME,
                "Ray-Ban", "100.00", 5, true
        );

        flushAndClear();

        // WHEN
        Optional<InventoryItem> result = repository.findBySkuIgnoreCase("mon-002");

        // THEN
        assertTrue(result.isPresent());

        assertEquals("MON-002", result.get().getSku());
    }

    // =====================================
    // 3. SKU NOT FOUND
    // =====================================

    @Test
    void givenUnknownSku_whenFind_thenReturnEmpty() {

        // WHEN
        Optional<InventoryItem> result = repository.findBySkuIgnoreCase("NO-EXISTE");

        // THEN
        assertTrue(result.isEmpty());
    }

    // =====================================
    // 4. EXISTS BY SKU IGNORE CASE
    // =====================================

    @Test
    void givenExistingSku_whenExists_thenReturnTrue() {

        // GIVEN
        createItem(
                "LEN-001", "Lente monofocal",
                InventoryItemType.LENS,
                "Essilor", "80.00", 20, true
        );

        flushAndClear();

        // WHEN + THEN
        assertTrue(repository.existsBySkuIgnoreCase("len-001"));
        assertFalse(repository.existsBySkuIgnoreCase("len-999"));
    }

    // =====================================
    // 5. FIND ACTIVE
    // =====================================

    @Test
    void givenActiveAndInactiveItems_whenFindActive_thenExcludeInactive() {

        // GIVEN
        createItem(
                "ACT-001", "Montura activa",
                InventoryItemType.FRAME,
                "Ray-Ban", "100.00", 5, true
        );

        createItem(
                "INA-001", "Montura inactiva",
                InventoryItemType.FRAME,
                "Ray-Ban", "100.00", 5, false
        );

        flushAndClear();

        // WHEN
        Page<InventoryItem> result = repository.findByActiveTrue(
                        PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("ACT-001", result.getContent().getFirst().getSku());
        assertTrue(result.getContent().getFirst().getActive());
    }

    // =====================================
    // 6. PAGINATION
    // =====================================

    @Test
    void givenThreeItems_whenFindActive_thenPaginate() {

        // GIVEN
        for (int i = 1; i <= 3; i++) {

            createItem(
                    "PAG-00" + i,
                    "Producto " + i,
                    InventoryItemType.FRAME,
                    "Ray-Ban",
                    "100.00",
                    10,
                    true
            );
        }

        flushAndClear();

        // WHEN
        Page<InventoryItem> result = repository.findByActiveTrue(PageRequest.of(
                0, 2, Sort.by("sku").ascending()));

        // THEN
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());

        assertEquals(List.of("PAG-001", "PAG-002"),
                result.getContent()
                        .stream()
                        .map(InventoryItem::getSku)
                        .toList());
    }

    // =====================================
    // 7. UNIQUE SKU
    // =====================================

    @Test
    void givenDuplicateSku_whenSave_thenDatabaseRejectsIt() {

        // GIVEN
        createItem(
                "DUP-001", "Montura original",
                InventoryItemType.FRAME,
                "Ray-Ban", "100.00", 5, true
        );

        InventoryItem duplicate = new InventoryItem();

        duplicate.setSku("DUP-001");
        duplicate.setName("Montura duplicada");
        duplicate.setType(InventoryItemType.FRAME);
        duplicate.setSalePrice(new BigDecimal("150.00"));
        duplicate.setStock(3);

        // WHEN + THEN
        assertThrows(PersistenceException.class, () -> entityManager.persist(duplicate));

        // El contexto de persistencia puede quedar
        // inutilizable después del error de restricción.
        // No realizamos más consultas en este test.
    }

    // =====================================
    // 8. SEARCH BY NAME AND BRAND
    // =====================================

    @Test
    void givenNameAndBrand_whenSearch_thenMatchIgnoringCase() {

        // GIVEN
        createItem(
                "MON-010", "Montura deportiva",
                InventoryItemType.FRAME,
                "Ray-Ban", "120.00", 8, true
        );

        createItem(
                "MON-011", "Montura clásica",
                InventoryItemType.FRAME,
                "Oakley", "110.00", 5, true
        );

        createItem(
                "LEN-010", "Lente premium",
                InventoryItemType.LENS,
                "Ray-Ban", "90.00", 10, true
        );

        flushAndClear();

        InventorySearchDto search = searchDto();

        search.setName("MONTURA");
        search.setBrand("ray-BAN");

        // WHEN
        Page<InventoryItem> result = executeSearch(search);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("MON-010", result.getContent().getFirst().getSku());
    }

    // =====================================
    // 9. SEARCH BY TYPE AND PRICE
    // =====================================

    @Test
    void givenTypeAndPriceRange_whenSearch_thenFilterCorrectly() {

        // GIVEN
        createItem(
                "FRA-001", "Montura económica",
                InventoryItemType.FRAME,
                "Marca A", "40.00", 10, true
        );

        createItem(
                "FRA-002", "Montura intermedia",
                InventoryItemType.FRAME,
                "Marca B", "120.00", 10, true
        );

        createItem(
                "FRA-003", "Montura premium",
                InventoryItemType.FRAME,
                "Marca C", "250.00", 10, true
        );

        createItem(
                "SUN-001", "Gafas de sol",
                InventoryItemType.SUNGLASSES,
                "Marca D", "120.00", 10, true
        );

        flushAndClear();

        InventorySearchDto search = searchDto();

        search.setType(InventoryItemType.FRAME);
        search.setMinPrice(new BigDecimal("50.00"));
        search.setMaxPrice(new BigDecimal("200.00"));

        // WHEN
        Page<InventoryItem> result = executeSearch(search);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("FRA-002", result.getContent().getFirst().getSku());
    }

    // =====================================
    // 10. IN STOCK TRUE
    // =====================================

    @Test
    void givenInStockTrue_whenSearch_thenExcludeOutOfStock() {

        // GIVEN
        createItem(
                "STK-001", "Montura disponible",
                InventoryItemType.FRAME,
                "Marca A", "100.00", 5, true
        );

        createItem(
                "STK-002", "Montura agotada",
                InventoryItemType.FRAME,
                "Marca B", "100.00", 0, true
        );

        flushAndClear();

        InventorySearchDto search = searchDto();

        search.setInStock(true);

        // WHEN
        Page<InventoryItem> result = executeSearch(search);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("STK-001", result.getContent().getFirst().getSku());
    }

    // =====================================
    // 11. IN STOCK FALSE
    // =====================================

    @Test
    void givenInStockFalse_whenSearch_thenReturnOnlyZeroStock() {

        // GIVEN
        createItem(
                "STK-003", "Montura disponible",
                InventoryItemType.FRAME,
                "Marca A", "100.00", 5, true
        );

        createItem(
                "STK-004", "Montura agotada",
                InventoryItemType.FRAME,
                "Marca B", "100.00", 0, true
        );

        flushAndClear();

        InventorySearchDto search = searchDto();

        search.setInStock(false);

        // WHEN
        Page<InventoryItem> result = executeSearch(search);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("STK-004", result.getContent().getFirst().getSku());
    }

    // =====================================
    // 12. SEARCH INACTIVE ITEMS
    // =====================================

    @Test
    void givenActiveFalse_whenSearch_thenReturnInactiveItems() {

        // GIVEN
        createItem(
                "ACT-010", "Artículo activo",
                InventoryItemType.FRAME,
                "Marca A", "100.00", 5, true
        );

        createItem(
                "INA-010", "Artículo inactivo",
                InventoryItemType.FRAME,
                "Marca B", "100.00", 5, false
        );

        flushAndClear();

        InventorySearchDto search = searchDto();

        search.setActive(false);

        // WHEN
        Page<InventoryItem> result = executeSearch(search);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("INA-010", result.getContent().getFirst().getSku());
    }

    // =====================================
    // 13. UPDATE STOCK
    // =====================================

    @Test
    void givenExistingItem_whenUpdateStock_thenPersistChange() {

        // GIVEN
        InventoryItem item = createItem(
                "UPD-001", "Montura",
                InventoryItemType.FRAME,
                "Marca A", "100.00", 10, true
        );

        Long id = item.getId();

        // WHEN
        item.setStock(7);

        repository.saveAndFlush(item);

        flushAndClear();

        // THEN
        InventoryItem updated = repository.findById(id).orElseThrow();

        assertEquals(7, updated.getStock());
        assertNotNull(updated.getUpdatedAt());
    }

    // =====================================
    // 14. DELETE
    // =====================================

    @Test
    void givenExistingItem_whenDelete_thenRemoveFromDatabase() {

        // GIVEN
        InventoryItem item = createItem(
                "DEL-001", "Montura eliminable",
                InventoryItemType.FRAME,
                "Marca A", "100.00", 3, true
        );

        Long id = item.getId();

        // WHEN
        repository.delete(item);

        flushAndClear();

        // THEN
        assertFalse(repository.existsById(id));
    }
}
