package com.optiflow.crm.inventory.movement.dto;

import com.optiflow.crm.inventory.movement.model.StockMovementType;
import jakarta.validation.constraints.*;

public record CreateStockMovementDto(
        @NotNull StockMovementType type,
        @NotNull @PositiveOrZero Integer quantity,
        @NotBlank @Size(max = 250) String reason
) {}
