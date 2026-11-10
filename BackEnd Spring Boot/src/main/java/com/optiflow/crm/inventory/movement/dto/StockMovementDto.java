package com.optiflow.crm.inventory.movement.dto;

import com.optiflow.crm.inventory.movement.model.StockMovementType;
import java.time.LocalDateTime;

public record StockMovementDto(
        Long id,
        Long inventoryItemId,
        Long userId,
        String userName,
        StockMovementType type,
        int previousStock,
        int newStock,
        int delta,
        String reason,
        LocalDateTime createdAt,
        String sourceType,
        Long sourceId
) {}
