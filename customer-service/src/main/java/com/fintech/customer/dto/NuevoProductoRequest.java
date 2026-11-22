package com.fintech.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

// Lo que envía el usuario para asociarle un producto a un cliente.
// El clienteId sale de la URL, no del body.
public record NuevoProductoRequest(
        @NotBlank
        @Pattern(regexp = "CUENTA|TARJETA|PRESTAMO|INVERSION",
                 message = "debe ser CUENTA, TARJETA, PRESTAMO o INVERSION")
        String tipo,
        @NotBlank String numero,
        String descripcion,
        @NotNull @PositiveOrZero BigDecimal monto) {
}
