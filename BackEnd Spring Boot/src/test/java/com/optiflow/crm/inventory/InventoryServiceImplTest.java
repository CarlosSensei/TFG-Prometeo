package com.optiflow.crm.inventory;

import com.optiflow.crm.inventory.dto.InventoryItemDto;
import com.optiflow.crm.inventory.dto.InventorySearchDto;
import com.optiflow.crm.inventory.mapper.InventoryItemMapper;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;
import com.optiflow.crm.shared.exception.InsufficientStockException;
import com.optiflow.crm.shared.exception.InventoryItemAlreadyExistsException;
import com.optiflow.crm.shared.exception.InventoryItemNotFoundException;

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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryItemMapper inventoryItemMapper;

    @InjectMocks
    private InventoryServiceImpl service;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private InventoryItem createItem(int stock) {

        InventoryItem item = new InventoryItem();

        item.setId(5L);
        item.setSku("MON-001");
        item.setName("Montura graduada");
        item.setType(InventoryItemType.FRAME);
        item.setSalePrice(new BigDecimal("80.00"));
        item.setTaxRate(new BigDecimal("21.00"));
        item.setStock(stock);
        item.setActive(true);

        return item;
    }

    private InventoryItemDto validDto() {

        InventoryItemDto dto = new InventoryItemDto();

        dto.setSku("MON-001");
        dto.setName("Montura graduada");
        dto.setType(InventoryItemType.FRAME);
        dto.setSalePrice(new BigDecimal("80.00"));
        dto.setTaxRate(new BigDecimal("21.00"));
        dto.setStock(10);

        return dto;
    }

    // =====================================
    // FIND ALL
    // =====================================

    @Test
    void givenActiveItems_whenFindAll_thenReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<InventoryItem> page = new PageImpl<>(List.of(createItem(10)), pageable, 1);

        when(inventoryRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<InventoryItem> result = service.findAll(pageable);

        assertSame(page, result);

        verify(inventoryRepository).findByActiveTrue(pageable);
    }

    // =====================================
    // SEARCH
    // =====================================

    @Test
    void givenSearch_whenSearch_thenUsePaginationAndSort() {

        InventorySearchDto search = new InventorySearchDto();

        search.setPage(2);
        search.setSize(5);
        search.setSortBy("name");
        search.setSortDirection("desc");

        when(inventoryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        // WHEN
        service.search(search);

        // THEN
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(inventoryRepository).findAll(any(Specification.class), captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());

        assertTrue(pageable.getSort().getOrderFor("name").isDescending());
    }

    // =====================================
    // FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturnItem() {

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        InventoryItem result = service.findById(5L);

        assertSame(item, result);
    }

    @Test
    void givenMissingId_whenFindById_thenThrow() {

        when(inventoryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(InventoryItemNotFoundException.class, () -> service.findById(5L));
    }

    // =====================================
    // CREATE
    // =====================================

    @Test
    void givenUniqueSku_whenCreate_thenActivateAndSave() {

        InventoryItemDto dto = validDto();
        InventoryItem entity = createItem(10);
        entity.setActive(false);

        when(inventoryRepository.existsBySkuIgnoreCase("MON-001")).thenReturn(false);
        when(inventoryItemMapper.toEntity(dto)).thenReturn(entity);
        when(inventoryRepository.save(entity)).thenReturn(entity);

        // WHEN
        InventoryItem result = service.create(dto);

        // THEN
        assertSame(entity, result);
        assertTrue(entity.getActive());

        verify(inventoryItemMapper).toEntity(dto);
        verify(inventoryRepository).save(entity);
    }

    @Test
    void givenDuplicateSku_whenCreate_thenThrowAndNeverSave() {

        InventoryItemDto dto = validDto();

        when(inventoryRepository.existsBySkuIgnoreCase("MON-001")).thenReturn(true);

        assertThrows(InventoryItemAlreadyExistsException.class, () -> service.create(dto));

        verify(inventoryRepository, never()).save(any(InventoryItem.class));

        verifyNoInteractions(inventoryItemMapper);
    }

    // =====================================
    // UPDATE
    // =====================================

    @Test
    void givenSameSku_whenUpdate_thenSaveWithoutDuplicateCheck() {

        InventoryItemDto dto = validDto();
        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(item)).thenReturn(item);

        // WHEN
        InventoryItem result = service.update(5L, dto);

        // THEN
        assertSame(item, result);

        verify(inventoryItemMapper).updateEntity(dto, item);
        verify(inventoryRepository, never()).existsBySkuIgnoreCase(anyString());
        verify(inventoryRepository).save(item);
    }

    @Test
    void givenNewAvailableSku_whenUpdate_thenSave() {

        InventoryItemDto dto = validDto();
        dto.setSku("MON-002");

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));
        when(inventoryRepository.existsBySkuIgnoreCase("MON-002")).thenReturn(false);
        when(inventoryRepository.save(item)).thenReturn(item);

        // WHEN
        InventoryItem result = service.update(5L, dto);

        // THEN
        assertSame(item, result);

        verify(inventoryRepository).existsBySkuIgnoreCase("MON-002");
        verify(inventoryItemMapper).updateEntity(dto, item);
        verify(inventoryRepository).save(item);
    }

    @Test
    void givenDuplicateSku_whenUpdate_thenThrow() {

        InventoryItemDto dto = validDto();
        dto.setSku("MON-002");

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));
        when(inventoryRepository.existsBySkuIgnoreCase("MON-002")).thenReturn(true);

        assertThrows(InventoryItemAlreadyExistsException.class, () ->
                service.update(5L, dto));

        verify(inventoryItemMapper, never()).updateEntity(any(), any());
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    // =====================================
    // DELETE
    // =====================================

    @Test
    void givenExistingItem_whenDelete_thenDeactivate() {

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        service.delete(5L);

        assertFalse(item.getActive());

        verify(inventoryRepository).save(item);
        verify(inventoryRepository, never()).delete(any(InventoryItem.class));
    }

    // =====================================
    // DECREASE STOCK
    // =====================================

    @Test
    void givenEnoughStock_whenDecrease_thenSubtractQuantity() {

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        // WHEN
        service.decreaseStock(5L, 3);

        // THEN
        assertEquals(7, item.getStock());

        verify(inventoryRepository).save(item);
    }

    @Test
    void givenExactStock_whenDecrease_thenStockBecomesZero() {

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        service.decreaseStock(5L, 10);

        assertEquals(0, item.getStock());

        verify(inventoryRepository).save(item);
    }

    @Test
    void givenInsufficientStock_whenDecrease_thenThrowWithoutSaving() {

        InventoryItem item = createItem(2);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        // WHEN + THEN
        assertThrows(InsufficientStockException.class, () ->
                service.decreaseStock(5L, 5));

        assertEquals(2, item.getStock());

        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void givenInvalidQuantity_whenDecrease_thenThrow() {

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () ->
                service.decreaseStock(5L, 0));

        assertEquals(10, item.getStock());

        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    // =====================================
    // INCREASE STOCK
    // =====================================

    @Test
    void givenValidQuantity_whenIncrease_thenAddStock() {

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        // WHEN
        service.increaseStock(5L, 5);

        // THEN
        assertEquals(15, item.getStock());

        verify(inventoryRepository).save(item);
    }

    @Test
    void givenInvalidQuantity_whenIncrease_thenThrow() {

        InventoryItem item = createItem(10);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () ->
                service.increaseStock(5L, -2));

        assertEquals(10, item.getStock());

        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    // =====================================
    // SERVICE ITEMS
    // =====================================

    @Test
    void givenServiceItem_whenDecrease_thenDoNotModifyStock() {

        InventoryItem item = createItem(0);
        item.setType(InventoryItemType.SERVICE);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        service.decreaseStock(5L, 3);

        assertEquals(0, item.getStock());

        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void givenServiceItem_whenIncrease_thenDoNotModifyStock() {

        InventoryItem item = createItem(0);
        item.setType(InventoryItemType.SERVICE);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        service.increaseStock(5L, 3);

        assertEquals(0, item.getStock());

        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    // =====================================
    // STOCK ITEM NOT FOUND
    // =====================================

    @Test
    void givenMissingItem_whenDecrease_thenThrow() {

        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InventoryItemNotFoundException.class, () ->
                service.decreaseStock(99L, 2));

        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }
}
