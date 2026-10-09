package com.optiflow.crm.shared.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {

    /*
        Situación	                    HTTP
        Recurso no existe	            404 NOT FOUND
        DTO inválido	                400 BAD REQUEST
        Estado de negocio incorrecto	400 BAD REQUEST
        Recurso duplicado	            409 CONFLICT
        Presupuesto ya convertido	    409 CONFLICT
        Constraint UNIQUE de PostgreSQL	409 CONFLICT
        Error inesperado	            500 INTERNAL SERVER ERROR

     */


}

