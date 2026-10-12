package com.optiflow.crm.shared.exception;

public class QuoteItemNotFoundException extends ResourceNotFoundException {

    public QuoteItemNotFoundException(Long id) {
        super("Línea de presupuesto no encontrada con id: " + id);
    }
}
