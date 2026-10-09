package com.optiflow.crm.shared.exception;

public class NotificationNotFoundException extends ResourceNotFoundException {

    public NotificationNotFoundException(Long id) {

        super("Notificación no encontrada con id: " + id);
    }
}
