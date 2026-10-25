package abaid.pedidos360.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record PedidoEvento(
        @NotNull Long pedidoId,
        @NotBlank String estado,
        @NotBlank String clienteUsername,
        @PositiveOrZero Double total,
        Instant timestamp
) {}
