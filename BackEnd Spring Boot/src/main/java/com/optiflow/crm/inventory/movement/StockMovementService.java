package com.optiflow.crm.inventory.movement;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;
import com.optiflow.crm.inventory.movement.dto.CreateStockMovementDto;
import com.optiflow.crm.inventory.movement.dto.StockMovementDto;
import com.optiflow.crm.inventory.movement.model.StockMovement;
import com.optiflow.crm.inventory.movement.model.StockMovementType;
import com.optiflow.crm.shared.exception.InsufficientStockException;
import com.optiflow.crm.shared.exception.InventoryItemNotFoundException;
import com.optiflow.crm.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final CurrentUserService currentUserService;

    public StockMovementService(StockMovementRepository movementRepository,
                                InventoryRepository inventoryRepository,
                                CurrentUserService currentUserService) {
        this.movementRepository = movementRepository;
        this.inventoryRepository = inventoryRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public StockMovementDto createManual(Long itemId, CreateStockMovementDto dto) {
        InventoryItem item = inventoryRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new InventoryItemNotFoundException(itemId));

        if (!Boolean.TRUE.equals(item.getActive())) {
            throw new IllegalArgumentException("No se puede modificar el stock de un artículo inactivo");
        }
        if (item.getType() == InventoryItemType.SERVICE) {
            throw new IllegalArgumentException("Los servicios no gestionan existencias");
        }
        if (dto.type() == null || dto.quantity() == null || dto.reason() == null || dto.reason().isBlank()) {
            throw new IllegalArgumentException("Tipo, cantidad y motivo son obligatorios");
        }
        if (dto.reason().trim().length() > 250) {
            throw new IllegalArgumentException("El motivo no puede superar 250 caracteres");
        }
        if (dto.quantity() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (dto.type() != com.optiflow.crm.inventory.movement.model.StockMovementType.ADJUSTMENT
                && dto.quantity() == 0) {
            throw new IllegalArgumentException("Entradas y salidas requieren una cantidad mayor que cero");
        }

        int before = item.getStock();
        int after;
        try {
            after = switch (dto.type()) {
                case ENTRY -> Math.addExact(before, dto.quantity());
                case EXIT -> Math.subtractExact(before, dto.quantity());
                case ADJUSTMENT -> dto.quantity(); // Existencia final contada
            };
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("El ajuste excede el rango de stock permitido");
        }
        if (after < 0) {
            throw new IllegalArgumentException("No hay existencias suficientes");
        }
        if (before == after) {
            throw new IllegalArgumentException("El movimiento debe modificar el stock");
        }

        User user = currentUserService.getCurrentUser();
        item.setStock(after); // Dirty checking, misma transacción que el movimiento

        StockMovement movement = new StockMovement();
        movement.setInventoryItem(item);
        movement.setUser(user);
        movement.setType(dto.type());
        movement.setPreviousStock(before);
        movement.setNewStock(after);
        movement.setDelta(after - before);
        movement.setReason(dto.reason().trim());

        return toDto(movementRepository.save(movement));
    }

    /**
     * Se invoca desde OrderServiceImpl, dentro de SU transacción @Transactional.
     * No sustituye las validaciones del workflow de OrderDomainService.
     * La constraint de BD impide apuntes duplicados para el mismo artículo/evento/pedido.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void applyOrderMovement(Long itemId, Long orderId, int quantity,
                                   StockMovementType type) {
        if (orderId == null || quantity <= 0 ||
                (type != StockMovementType.EXIT && type != StockMovementType.ENTRY)) {
            throw new IllegalArgumentException("Pedido, cantidad y tipo de movimiento no válidos");
        }

        String sourceType = type == StockMovementType.EXIT
                ? "ORDER_CONFIRM" : "ORDER_CANCEL";

        // El lock se comparte con el que usan movimientos manuales.
        InventoryItem item = inventoryRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new InventoryItemNotFoundException(itemId));

        if (item.getType() == InventoryItemType.SERVICE) {
            return; // Los servicios no tienen stock ni generan apuntes.
        }

        if (movementRepository.existsByInventoryItemIdAndSourceTypeAndSourceId(
                itemId, sourceType, orderId)) {
            throw new IllegalStateException("El movimiento del pedido ya está registrado");
        }

        if (type == StockMovementType.EXIT && !Boolean.TRUE.equals(item.getActive())) {
            throw new IllegalArgumentException("No se puede confirmar un artículo inactivo");
        }

        int before = item.getStock();
        int after;
        try {
            after = type == StockMovementType.EXIT
                    ? Math.subtractExact(before, quantity)
                    : Math.addExact(before, quantity);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("El stock excede el rango permitido", ex);
        }
        if (after < 0) {
            throw new InsufficientStockException(itemId, before, quantity);
        }

        User actor = currentUserService.getCurrentUser();
        item.setStock(after);

        StockMovement movement = new StockMovement();
        movement.setInventoryItem(item);
        movement.setUser(actor);
        movement.setType(type);
        movement.setPreviousStock(before);
        movement.setNewStock(after);
        movement.setDelta(after - before);
        movement.setSourceType(sourceType);
        movement.setSourceId(orderId);
        movement.setReason(type == StockMovementType.EXIT
                ? "Confirmación del pedido #" + orderId
                : "Cancelación del pedido #" + orderId);
        movementRepository.save(movement);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementDto> findByItem(Long itemId, int page, int size) {
        if (!inventoryRepository.existsById(itemId)) {
            throw new InventoryItemNotFoundException(itemId);
        }
        Pageable pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt", "id"));
        return movementRepository.findByInventoryItemId(itemId, pageable).map(this::toDto);
    }

    private StockMovementDto toDto(StockMovement movement) {
        User user = movement.getUser();
        return new StockMovementDto(
                movement.getId(), movement.getInventoryItem().getId(), user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                movement.getType(), movement.getPreviousStock(), movement.getNewStock(),
                movement.getDelta(), movement.getReason(), movement.getCreatedAt(),
                movement.getSourceType(), movement.getSourceId());
    }
}
