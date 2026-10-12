package com.optiflow.crm.inventory;

import com.optiflow.crm.inventory.dto.InventoryItemDto;
import com.optiflow.crm.inventory.dto.InventorySearchDto;
import com.optiflow.crm.inventory.mapper.InventoryItemMapper;
import com.optiflow.crm.inventory.model.InventoryItem;

import com.optiflow.crm.inventory.model.InventoryService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

// Controller de inventary items
@RestController
@RequestMapping("/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    // Inyeccion de service y mapper
    private final InventoryService inventoryService;
    private final InventoryItemMapper inventoryItemMapper;

    public InventoryController(
            InventoryService inventoryService,
            InventoryItemMapper inventoryItemMapper
    ) {
        this.inventoryService = inventoryService;
        this.inventoryItemMapper = inventoryItemMapper;
    }

    // Busqueda paginada de inventory items
    @GetMapping
    public ResponseEntity<Page<InventoryItemDto>> findAll(Pageable pageable) {

        Page<InventoryItemDto> items = inventoryService.findAll(pageable).map(inventoryItemMapper::toDto);

        return ResponseEntity.ok(items);
    }

    // Busqueda por id
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDto> findById(@PathVariable Long id) {

        InventoryItem item = inventoryService.findById(id);

        return ResponseEntity.ok(inventoryItemMapper.toDto(item));
    }

    // Busqueda personalizada
    @PostMapping("/search")
    public ResponseEntity<Page<InventoryItemDto>> search(@RequestBody InventorySearchDto searchDto) {

        Page<InventoryItemDto> items = inventoryService.search(searchDto).map(inventoryItemMapper::toDto);

        return ResponseEntity.ok(items);
    }

    // Crear un item nuevo
    @PostMapping
    public ResponseEntity<InventoryItemDto> create(@Valid @RequestBody InventoryItemDto dto) {

        InventoryItem item = inventoryService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryItemMapper.toDto(item));
    }

    // Actualizar un item
    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody InventoryItemDto dto) {

        InventoryItem item = inventoryService.update(id, dto);

        return ResponseEntity.ok(inventoryItemMapper.toDto(item));
    }

    // Borrar un item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        inventoryService.delete(id);

        return ResponseEntity.noContent().build();
    }
}