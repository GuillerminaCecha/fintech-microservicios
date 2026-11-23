package com.fintech.product.dto;

import com.fintech.product.model.EstadoProducto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

// Lo que se puede MODIFICAR de un producto. El número, el tipo y el cliente no cambian.
public record ProductoUpdateRequest(
        String descripcion,
        @NotNull @PositiveOrZero BigDecimal monto,
        @NotNull(message = "es obligatorio (ACTIVO o INACTIVO)") EstadoProducto estado) {
}
