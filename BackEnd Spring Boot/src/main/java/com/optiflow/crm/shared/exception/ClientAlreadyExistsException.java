package com.optiflow.crm.shared.exception;

public class ClientAlreadyExistsException extends DuplicateResourceException {

    public ClientAlreadyExistsException(String DNI) {
        super("Ya existe un cliente con dni: " + DNI);
    }
}
