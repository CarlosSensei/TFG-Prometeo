package com.optiflow.crm.inventory.mapper;

import com.optiflow.crm.inventory.dto.InventoryItemDto;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InventoryItemMapperTest {

    private InventoryItemMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InventoryItemMapper();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private InventoryItem completeItem() {

        InventoryItem item = new InventoryItem();

        item.setId(1L);
        item.setSku("MON-001");
        item.setName("Montura graduada");
        item.setType(InventoryItemType.FRAME);
        item.setBrand("Ray-Ban");
        item.setDescription("Montura de acetato");

        item.setSalePrice(new BigDecimal("120.00"));
        item.setTaxRate(new BigDecimal("21.00"));

        item.setStock(10);
        item.setActive(true);

        item.setCreatedAt(LocalDateTime.of(2026, 10, 1, 10, 0));
        item.setUpdatedAt(LocalDateTime.of(2026, 10, 2, 12, 0));

        return item;
    }

    private InventoryItemDto completeDto() {

        InventoryItemDto dto = new InventoryItemDto();

        dto.setId(99L);
        dto.setSku("LEN-002");
        dto.setName("Lentes progresivas");
        dto.setType(InventoryItemType.LENS);
        dto.setBrand("Essilor");
        dto.setDescription("Lentes progresivas premium");

        dto.setSalePrice(new BigDecimal("250.00"));
        dto.setTaxRate(new BigDecimal("21.00"));

        dto.setStock(20);
        dto.setActive(false);

        dto.setCreatedAt(LocalDateTime.of(2026, 9, 1, 9, 0));
        dto.setUpdatedAt(LocalDateTime.of(2026, 9, 5, 15, 0));

        return dto;
    }

    // =====================================
    // 1. TO DTO - CONVERSIÓN COMPLETA
    // =====================================

    @Test
    void givenCompleteItem_whenToDto_thenMapAllFields() {

        // GIVEN
        InventoryItem item = completeItem();

        // WHEN
        InventoryItemDto dto = mapper.toDto(item);

        // THEN
        assertAll("InventoryItem -> InventoryItemDto",
                () -> assertEquals(item.getId(), dto.getId()),
                () -> assertEquals(item.getSku(), dto.getSku()),
                () -> assertEquals(item.getName(), dto.getName()),
                () -> assertEquals(item.getType(), dto.getType()),
                () -> assertEquals(item.getBrand(), dto.getBrand()),
                () -> assertEquals(item.getDescription(), dto.getDescription()),
                () -> assertEquals(item.getSalePrice(), dto.getSalePrice()),
                () -> assertEquals(item.getTaxRate(), dto.getTaxRate()),
                () -> assertEquals(item.getStock(), dto.getStock()),
                () -> assertEquals(item.getActive(), dto.getActive()),
                () -> assertEquals(item.getCreatedAt(), dto.getCreatedAt()),
                () -> assertEquals(item.getUpdatedAt(), dto.getUpdatedAt()));
    }

    // =====================================
    // 2. TO DTO - CAMPOS OPCIONALES NULOS
    // =====================================

    @Test
    void givenNullOptionalFields_whenToDto_thenPreserveNulls() {

        // GIVEN
        InventoryItem item = completeItem();

        item.setBrand(null);
        item.setDescription(null);
        item.setCreatedAt(null);
        item.setUpdatedAt(null);

        // WHEN
        InventoryItemDto dto = mapper.toDto(item);

        // THEN
        assertAll(
                () -> assertNull(dto.getBrand()),
                () -> assertNull(dto.getDescription()),
                () -> assertNull(dto.getCreatedAt()),
                () -> assertNull(dto.getUpdatedAt()));
    }

    // =====================================
    // 3. TO ENTITY - CONVERSIÓN COMPLETA
    // =====================================

    @Test
    void givenCompleteDto_whenToEntity_thenMapEditableFields() {

        // GIVEN
        InventoryItemDto dto = completeDto();

        // WHEN
        InventoryItem item = mapper.toEntity(dto);

        // THEN
        assertNotNull(item);

        assertAll("InventoryItemDto -> InventoryItem",
                () -> assertEquals(dto.getSku(), item.getSku()),
                () -> assertEquals(dto.getName(), item.getName()),
                () -> assertEquals(dto.getType(), item.getType()),
                () -> assertEquals(dto.getBrand(), item.getBrand()),
                () -> assertEquals(dto.getDescription(), item.getDescription()),
                () -> assertEquals(dto.getSalePrice(), item.getSalePrice()),
                () -> assertEquals(dto.getTaxRate(), item.getTaxRate()),
                () -> assertEquals(dto.getStock(), item.getStock()));
    }

    // =====================================
    // 4. TO ENTITY - CAMPOS PROTEGIDOS
    // =====================================

    @Test
    void givenDtoWithControlledFields_whenToEntity_thenIgnoreThem() {

        // GIVEN
        InventoryItemDto dto = completeDto();

        // WHEN
        InventoryItem item = mapper.toEntity(dto);

        // THEN
        assertNull(item.getId());

        // La entidad tiene active=true por defecto.
        assertTrue(item.getActive());

        // La auditoría se genera mediante JPA.
        assertNull(item.getCreatedAt());
        assertNull(item.getUpdatedAt());

        // Los valores anteriores del DTO se ignoran.
        assertNotEquals(dto.getActive(), item.getActive());
    }

    // =====================================
    // 5. TO ENTITY - SERVICIOS
    // =====================================

    @Test
    void givenServiceDto_whenToEntity_thenPreserveServiceType() {

        // GIVEN
        InventoryItemDto dto = completeDto();

        dto.setType(InventoryItemType.SERVICE);
        dto.setStock(0);

        // WHEN
        InventoryItem item = mapper.toEntity(dto);

        // THEN
        assertEquals(InventoryItemType.SERVICE, item.getType());
        assertEquals(0, item.getStock());
    }

    // =====================================
    // 6. UPDATE - CAMPOS EDITABLES
    // =====================================

    @Test
    void givenDto_whenUpdateEntity_thenUpdateEditableFields() {

        // GIVEN
        InventoryItem existing = completeItem();
        InventoryItemDto dto = completeDto();

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertAll("Campos editables",
                () -> assertEquals(dto.getSku(), existing.getSku()),
                () -> assertEquals(dto.getName(), existing.getName()),
                () -> assertEquals(dto.getType(), existing.getType()),
                () -> assertEquals(dto.getBrand(), existing.getBrand()),
                () -> assertEquals(dto.getDescription(), existing.getDescription()),
                () -> assertEquals(dto.getSalePrice(), existing.getSalePrice()),
                () -> assertEquals(dto.getTaxRate(), existing.getTaxRate()),
                () -> assertEquals(dto.getStock(), existing.getStock()));
    }

    // =====================================
    // 7. UPDATE - CAMPOS PROTEGIDOS
    // =====================================

    @Test
    void givenDto_whenUpdateEntity_thenPreserveControlledFields() {

        // GIVEN
        InventoryItem existing = completeItem();
        InventoryItemDto dto = completeDto();

        Long originalId = existing.getId();
        Boolean originalActive = existing.getActive();

        LocalDateTime originalCreated = existing.getCreatedAt();
        LocalDateTime originalUpdated = existing.getUpdatedAt();

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertAll("Campos protegidos",
                () -> assertEquals(originalId, existing.getId()),
                () -> assertEquals(originalActive, existing.getActive()),
                () -> assertEquals(originalCreated, existing.getCreatedAt()),
                () -> assertEquals(originalUpdated, existing.getUpdatedAt()));
    }

    // =====================================
    // 8. UPDATE - CAMPOS NULOS
    // =====================================

    @Test
    void givenNullFields_whenUpdateEntity_thenOverwriteWithNull() {

        // GIVEN
        InventoryItem existing = completeItem();
        InventoryItemDto dto = completeDto();

        dto.setBrand(null);
        dto.setDescription(null);

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertNull(existing.getBrand());
        assertNull(existing.getDescription());
    }

    // =====================================
    // 9. STOCK CERO
    // =====================================

    @Test
    void givenZeroStock_whenMap_thenPreserveZero() {

        // GIVEN
        InventoryItemDto dto = completeDto();
        dto.setStock(0);

        // WHEN
        InventoryItem item = mapper.toEntity(dto);
        InventoryItemDto result = mapper.toDto(item);

        // THEN
        assertEquals(0, item.getStock());
        assertEquals(0, result.getStock());
    }
}
