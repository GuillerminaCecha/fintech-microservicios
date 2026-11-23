package com.fintech.customer.exception;

public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(Long id) {
        super("No se encontró el cliente con id " + id);
    }
}
