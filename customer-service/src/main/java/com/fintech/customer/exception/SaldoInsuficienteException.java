package com.fintech.customer.exception;

import java.math.BigDecimal;

// Regla de negocio: se responde con 422 Unprocessable Content
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal saldo, BigDecimal monto) {
        super("Saldo insuficiente: saldo actual " + saldo + ", monto solicitado " + monto);
    }
}
