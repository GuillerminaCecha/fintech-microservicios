package com.fintech.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nombre,
        String apellido,
        String documento,
        String email,
        BigDecimal saldo,
        LocalDateTime fechaAlta) {
}
