package com.optiflow.crm.inventory.movement;

import com.optiflow.crm.inventory.movement.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    boolean existsByInventoryItemIdAndSourceTypeAndSourceId(
            Long inventoryItemId, String sourceType, Long sourceId);

    Page<StockMovement> findByInventoryItemId(Long inventoryItemId, Pageable pageable);
}
