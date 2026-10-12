package com.optiflow.crm.inventory;

import com.optiflow.crm.inventory.model.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

// Repository de Inventory Items
public interface InventoryRepository extends JpaRepository<InventoryItem, Long>,
        JpaSpecificationExecutor<InventoryItem> {

    Page<InventoryItem> findByActiveTrue(Pageable pageable);

    Optional<InventoryItem> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);
}