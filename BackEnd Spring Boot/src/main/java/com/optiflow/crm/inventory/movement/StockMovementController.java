package com.optiflow.crm.inventory.movement;

import com.optiflow.crm.inventory.movement.dto.CreateStockMovementDto;
import com.optiflow.crm.inventory.movement.dto.StockMovementDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/inventory/{itemId}/movements")
public class StockMovementController {

    private final StockMovementService service;

    public StockMovementController(StockMovementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<StockMovementDto>> findAll(
            @PathVariable @Positive Long itemId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(service.findByItem(itemId, page, size));
    }

    @PostMapping
    public ResponseEntity<StockMovementDto> create(
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody CreateStockMovementDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createManual(itemId, dto));
    }
}
