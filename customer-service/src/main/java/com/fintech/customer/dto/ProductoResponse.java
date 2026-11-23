package com.fintech.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Contrato con product-service: lo que devuelve GET /productos/cliente/{id}.
// Es una copia del DTO de product-service: los microservicios NO comparten clases,
// comparten el contrato JSON (Clase 22).
public record ProductoResponse(
        Long id,
        String numero,
        String tipo,
        String descripcion,
        BigDecimal monto,
        String estado,
        Long clienteId,
        LocalDateTime fechaAlta) {
}
