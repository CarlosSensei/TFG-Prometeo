package com.optiflow.crm.shared.exception;

public class InventoryItemAlreadyExistsException extends DuplicateResourceException {

    public InventoryItemAlreadyExistsException(String sku) {
        super("Ya existe un artículo con SKU: " + sku);
    }
}