package com.optiflow.crm.inventory.movement.model;

import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements", uniqueConstraints = {
    @UniqueConstraint(name = "uk_stock_movement_order_event", columnNames = {"inventory_item_id", "source_type", "source_id"})
}, indexes = {
    @Index(name = "idx_stock_movements_item_created", columnList = "inventory_item_id,created_at")
})
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StockMovementType type;

    @Column(nullable = false)
    private int previousStock;

    @Column(nullable = false)
    private int newStock;

    @Column(nullable = false)
    private int delta;

    @Column(nullable = false, length = 250)
    private String reason;

    // Null para movimientos manuales; ORDER_CONFIRM u ORDER_CANCEL para pedidos.
    @Column(name = "source_type", length = 30)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public Long getId() { return id; }
    public InventoryItem getInventoryItem() { return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public StockMovementType getType() { return type; }
    public void setType(StockMovementType type) { this.type = type; }
    public int getPreviousStock() { return previousStock; }
    public void setPreviousStock(int previousStock) { this.previousStock = previousStock; }
    public int getNewStock() { return newStock; }
    public void setNewStock(int newStock) { this.newStock = newStock; }
    public int getDelta() { return delta; }
    public void setDelta(int delta) { this.delta = delta; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
