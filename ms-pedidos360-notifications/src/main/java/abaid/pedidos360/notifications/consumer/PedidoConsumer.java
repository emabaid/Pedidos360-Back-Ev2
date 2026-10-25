package abaid.pedidos360.notifications.consumer;

import abaid.pedidos360.notifications.dto.PedidoEvento;
import abaid.pedidos360.notifications.messaging.MensajeProcesador;
import abaid.pedidos360.notifications.messaging.RabbitNames;
import abaid.pedidos360.notifications.service.NotificacionService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Consumidores del dominio pedido: pedido creado y pedido aceptado
@Component
public class PedidoConsumer {

    private final MensajeProcesador procesador;
    private final NotificacionService notificaciones;

    public PedidoConsumer(MensajeProcesador procesador, NotificacionService notificaciones) {
        this.procesador = procesador;
        this.notificaciones = notificaciones;
    }

    @RabbitListener(id = "pedidoCreadoListener", queues = RabbitNames.Q_CREADOS)
    public void onPedidoCreado(Message message, Channel channel) throws IOException {
        procesador.procesar(message, channel, PedidoEvento.class, notificaciones::notificarCreado);
    }

    @RabbitListener(id = "pedidoAceptadoListener", queues = RabbitNames.Q_ACEPTADOS)
    public void onPedidoAceptado(Message message, Channel channel) throws IOException {
        procesador.procesar(message, channel, PedidoEvento.class, notificaciones::notificarAceptado);
    }
}
