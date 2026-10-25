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

// Consumidor del dominio despacho: pedido despachado
@Component
public class DespachoConsumer {

    private final MensajeProcesador procesador;
    private final NotificacionService notificaciones;

    public DespachoConsumer(MensajeProcesador procesador, NotificacionService notificaciones) {
        this.procesador = procesador;
        this.notificaciones = notificaciones;
    }

    @RabbitListener(id = "pedidoDespachadoListener", queues = RabbitNames.Q_DESPACHADOS)
    public void onPedidoDespachado(Message message, Channel channel) throws IOException {
        procesador.procesar(message, channel, PedidoEvento.class, notificaciones::notificarDespachado);
    }
}
