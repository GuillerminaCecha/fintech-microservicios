package com.fintech.customer.exception;

// Se responde con 409 Conflict
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
