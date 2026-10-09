package com.optiflow.crm.shared.exception;

public class ClientNotFoundException extends ResourceNotFoundException {

    public ClientNotFoundException(Long id) {

        super("Cliente no encontrado con id: " + id);
    }
}