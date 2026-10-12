package com.optiflow.crm.inventory.dto;

import com.optiflow.crm.inventory.model.InventoryItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryItemDto {

    private Long id;

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50)
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String name;

    @NotNull(message = "El tipo es obligatorio")
    private InventoryItemType type;

    @Size(max = 100)
    private String brand;

    @Size(max = 500)
    private String description;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal salePrice;

    @NotNull(message = "El IVA es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal taxRate;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero
    private Integer stock;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InventoryItemType getType() {
        return type;
    }

    public void setType(InventoryItemType type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}