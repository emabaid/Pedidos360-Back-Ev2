package abaid.pedidos360.orders.messaging;

import abaid.pedidos360.orders.model.EstadoPedido;
import abaid.pedidos360.orders.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PedidoEventoProducerTest {

    private final RabbitTemplate template = mock(RabbitTemplate.class);
    private final PedidoEventoProducer producer = new PedidoEventoProducer(template);

    private Order pedido(EstadoPedido estado) {
        Order o = new Order();
        o.setId(5L);
        o.setClienteUsername("ana@test.cl");
        o.setEstado(estado);
        return o;
    }

    @Test
    void publicaConLaRoutingKeyDelEstado() {
        producer.publicar(pedido(EstadoPedido.ACEPTADO));
        verify(template).convertAndSend(eq(RabbitNames.EXCHANGE), eq(RabbitNames.RK_PEDIDO_ACEPTADO), any(PedidoEvento.class));
    }

    @Test
    void estadoSinEvento_noPublica() {
        producer.publicar(pedido(EstadoPedido.ENTREGADO));
        verify(template, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void siElBrokerFalla_noLanzaExcepcion() {
        doThrow(new AmqpConnectException(new RuntimeException("down")))
                .when(template).convertAndSend(anyString(), anyString(), any(Object.class));
        assertDoesNotThrow(() -> producer.publicar(pedido(EstadoPedido.CREADO)));
    }
}
