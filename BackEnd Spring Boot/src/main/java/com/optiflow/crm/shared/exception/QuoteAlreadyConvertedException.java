package com.optiflow.crm.shared.exception;

public class QuoteAlreadyConvertedException extends RuntimeException {

    public QuoteAlreadyConvertedException(Long quoteId) {

        super("El presupuesto con id " + quoteId + " ya ha sido convertido en pedido");
    }
}