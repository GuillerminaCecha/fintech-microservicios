package com.fintech.product.dto;

import com.fintech.product.model.TipoProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

// Lo que llega para CREAR un producto (lo envía customer-service vía Feign o un cliente REST)
public record ProductoRequest(
        @NotNull(message = "es obligatorio (CUENTA, TARJETA, PRESTAMO o INVERSION)") TipoProducto tipo,
        @NotBlank String numero,
        String descripcion,
        @NotNull @PositiveOrZero BigDecimal monto,
        @NotNull @Positive Long clienteId) {
}
