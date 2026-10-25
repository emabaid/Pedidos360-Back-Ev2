package abaid.pedidos360.orders.messaging;

import java.time.Instant;

public record PedidoEvento(
        Long pedidoId,
        String estado,
        String clienteUsername,
        Double total,
        Instant timestamp
) {}
