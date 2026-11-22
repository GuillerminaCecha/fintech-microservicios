package com.fintech.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Actualización de datos personales. El saldo NO se puede editar acá:
// solo cambia con /depositar y /extraer (corrección del medio término)
public record ClienteUpdateRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Size(max = 20) String documento,
        @NotBlank @Email String email) {
}
