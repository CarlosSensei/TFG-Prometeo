package com.optiflow.crm.shared.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long itemId, Integer available, Integer requested) {
        super("Stock insuficiente para el artículo " + itemId
                        + ". Disponible: " + available
                        + ", solicitado: " + requested);
    }
}