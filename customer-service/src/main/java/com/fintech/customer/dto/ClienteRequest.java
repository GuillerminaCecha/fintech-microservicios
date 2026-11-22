package com.fintech.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// Alta de cliente: el saldo se informa solo como saldo INICIAL
public record ClienteRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Size(max = 20) String documento,
        @NotBlank @Email String email,
        @NotNull @PositiveOrZero BigDecimal saldo) {
}
