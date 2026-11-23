package com.fintech.customer.exception;

import java.time.LocalDateTime;

// Formato JSON único para todos los errores de la API
public record ErrorRespuesta(LocalDateTime fecha, int status, String mensaje) {

    public ErrorRespuesta(int status, String mensaje) {
        this(LocalDateTime.now(), status, mensaje);
    }
}
