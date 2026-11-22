package com.fintech.customer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MontoRequest(@NotNull @Positive BigDecimal monto) {
}
