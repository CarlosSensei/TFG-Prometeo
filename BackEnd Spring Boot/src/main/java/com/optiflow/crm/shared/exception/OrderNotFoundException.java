package com.optiflow.crm.shared.exception;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {

        super("Pedido no encontrado con id: " + id);
    }
}