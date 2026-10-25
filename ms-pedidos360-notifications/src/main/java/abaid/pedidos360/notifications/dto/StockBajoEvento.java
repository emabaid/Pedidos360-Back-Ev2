package abaid.pedidos360.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record StockBajoEvento(
        @NotNull Long productoId,
        @NotBlank String nombre,
        @NotNull @PositiveOrZero Integer stock,
        Integer umbral,
        Instant timestamp
) {}
