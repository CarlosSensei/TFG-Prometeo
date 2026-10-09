package com.optiflow.crm.shared.exception;

public class FollowUpNotFoundException extends ResourceNotFoundException {

    public FollowUpNotFoundException(Long id) {

        super("Seguimiento no encontrado con id: " + id);
    }
}