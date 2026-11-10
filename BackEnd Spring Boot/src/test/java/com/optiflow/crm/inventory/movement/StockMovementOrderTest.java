package com.optiflow.crm.inventory.movement;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;
import com.optiflow.crm.inventory.movement.model.StockMovement;
import com.optiflow.crm.inventory.movement.model.StockMovementType;
import com.optiflow.crm.shared.exception.InsufficientStockException;
import com.optiflow.crm.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementOrderTest {

    @Mock StockMovementRepository movements;
    @Mock InventoryRepository inventory;
    @Mock CurrentUserService currentUser;
    @InjectMocks StockMovementService service;

    private InventoryItem stocked(int stock) {
        InventoryItem item = new InventoryItem();
        item.setId(5L);
        item.setType(InventoryItemType.FRAME);
        item.setActive(true);
        item.setStock(stock);
        when(inventory.findByIdForUpdate(5L)).thenReturn(Optional.of(item));
        return item;
    }

    @Test
    void confirmationDeductsAndWritesOneAuditedMovement() {
        InventoryItem item = stocked(10);
        when(currentUser.getCurrentUser()).thenReturn(new User());
        service.applyOrderMovement(5L, 21L, 3, StockMovementType.EXIT);

        assertEquals(7, item.getStock());
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movements).save(captor.capture());
        StockMovement m = captor.getValue();
        assertEquals(-3, m.getDelta());
        assertEquals(10, m.getPreviousStock());
        assertEquals(7, m.getNewStock());
        assertEquals("ORDER_CONFIRM", m.getSourceType());
        assertEquals(21L, m.getSourceId());
    }

    @Test
    void cancellationRestoresAndWritesMovement() {
        InventoryItem item = stocked(7);
        when(currentUser.getCurrentUser()).thenReturn(new User());
        service.applyOrderMovement(5L, 21L, 3, StockMovementType.ENTRY);
        assertEquals(10, item.getStock());
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movements).save(captor.capture());
        assertEquals("ORDER_CANCEL", captor.getValue().getSourceType());
        assertEquals(3, captor.getValue().getDelta());
    }

    @Test
    void insufficientStockDoesNotWriteMovement() {
        stocked(2);
        assertThrows(InsufficientStockException.class,
                () -> service.applyOrderMovement(5L, 21L, 3, StockMovementType.EXIT));
        verify(movements, never()).save(any());
    }

    @Test
    void duplicateEventIsRejectedBeforeWriting() {
        stocked(10);
        when(movements.existsByInventoryItemIdAndSourceTypeAndSourceId(
                5L, "ORDER_CONFIRM", 21L)).thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> service.applyOrderMovement(5L, 21L, 3, StockMovementType.EXIT));
        verify(movements, never()).save(any());
    }

    @Test
    void servicesNeverMoveStock() {
        InventoryItem item = stocked(0);
        item.setType(InventoryItemType.SERVICE);
        service.applyOrderMovement(5L, 21L, 2, StockMovementType.EXIT);
        assertEquals(0, item.getStock());
        verifyNoInteractions(movements, currentUser);
    }
}
