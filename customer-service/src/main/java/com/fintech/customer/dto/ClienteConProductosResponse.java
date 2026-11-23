package com.fintech.customer.dto;

import java.util.List;

// Combina datos de los dos microservicios en una sola respuesta
public record ClienteConProductosResponse(
        ClienteResponse cliente,
        List<ProductoResponse> productos) {
}
