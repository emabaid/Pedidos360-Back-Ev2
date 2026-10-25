package abaid.pedidos360.notifications.consumer;

import abaid.pedidos360.notifications.dto.StockBajoEvento;
import abaid.pedidos360.notifications.messaging.MensajeProcesador;
import abaid.pedidos360.notifications.messaging.RabbitNames;
import abaid.pedidos360.notifications.service.NotificacionService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Consumidor del dominio inventario: avisos de stock bajo que publica catalog
@Component
public class InventarioConsumer {

    private final MensajeProcesador procesador;
    private final NotificacionService notificaciones;

    public InventarioConsumer(MensajeProcesador procesador, NotificacionService notificaciones) {
        this.procesador = procesador;
        this.notificaciones = notificaciones;
    }

    @RabbitListener(id = "stockBajoListener", queues = RabbitNames.Q_STOCK_BAJO)
    public void onStockBajo(Message message, Channel channel) throws IOException {
        procesador.procesar(message, channel, StockBajoEvento.class, notificaciones::notificarStockBajo);
    }
}
