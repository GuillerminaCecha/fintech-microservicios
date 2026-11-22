package com.fintech.product.dto;

import com.fintech.product.model.EstadoProducto;
import com.fintech.product.model.TipoProducto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Lo que DEVUELVE la API: nunca se expone la entidad directamente (Clase 22)
public record ProductoResponse(
        Long id,
        String numero,
        TipoProducto tipo,
        String descripcion,
        BigDecimal monto,
        EstadoProducto estado,
        Long clienteId,
        LocalDateTime fechaAlta) {
}
