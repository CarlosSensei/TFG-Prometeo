package com.optiflow.crm.shared.exception;

public class UserAlreadyCreatedException extends DuplicateResourceException {

    public UserAlreadyCreatedException(String email) {
        super("Ya existe un usuario con el email " + email);
    }
}
