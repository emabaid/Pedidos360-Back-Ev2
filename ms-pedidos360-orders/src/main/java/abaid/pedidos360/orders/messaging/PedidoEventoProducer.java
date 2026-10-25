package abaid.pedidos360.orders.messaging;

import abaid.pedidos360.orders.model.EstadoPedido;
import abaid.pedidos360.orders.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

// Si RabbitMQ no responde solo se escribe el error en el log: el pedido ya se guardó igual.
@Component
public class PedidoEventoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventoProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public PedidoEventoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(Order order) {
        String routingKey = routingKeyPara(order.getEstado());
        if (routingKey == null) {
            return; // estados sin evento (EN_PREPARACION, ENTREGADO, CANCELADO)
        }
        double total = order.getItems().stream()
                .mapToDouble(i -> i.getPrecioUnitario() * i.getCantidad())
                .sum();
        PedidoEvento evento = new PedidoEvento(
                order.getId(), order.getEstado().name(), order.getClienteUsername(), total, Instant.now());
        try {
            rabbitTemplate.convertAndSend(RabbitNames.EXCHANGE, routingKey, evento);
            log.info("Evento publicado rk={} pedido={}", routingKey, order.getId());
        } catch (Exception e) {
            log.error("No se pudo publicar evento rk={} pedido={}: {}", routingKey, order.getId(), e.getMessage());
        }
    }

    private String routingKeyPara(EstadoPedido estado) {
        return switch (estado) {
            case CREADO -> RabbitNames.RK_PEDIDO_CREADO;
            case ACEPTADO -> RabbitNames.RK_PEDIDO_ACEPTADO;
            case DESPACHADO -> RabbitNames.RK_PEDIDO_DESPACHADO;
            default -> null;
        };
    }
}
