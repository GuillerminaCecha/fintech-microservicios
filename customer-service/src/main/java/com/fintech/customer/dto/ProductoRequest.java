package com.fintech.customer.dto;

import java.math.BigDecimal;

// Contrato con product-service: es el body que se envía por Feign a POST /productos
public record ProductoRequest(
        String tipo,
        String numero,
        String descripcion,
        BigDecimal monto,
        Long clienteId) {
}
