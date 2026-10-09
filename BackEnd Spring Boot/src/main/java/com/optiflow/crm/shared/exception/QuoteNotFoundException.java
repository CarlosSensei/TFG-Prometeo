package com.optiflow.crm.shared.exception;

public class QuoteNotFoundException extends ResourceNotFoundException {

    public QuoteNotFoundException(Long id) {

        super("Presupuesto no encontrado con id: " + id);
    }
}