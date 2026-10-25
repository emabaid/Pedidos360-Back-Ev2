package abaid.pedidos360.notifications.consumer;

import abaid.pedidos360.notifications.messaging.RabbitNames;
import abaid.pedidos360.notifications.service.DeadLetterService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Lee las DLQs: escribe el mensaje fallido en el log y lo guarda en la BD.
// Siempre confirma (ACK): si la BD falla, el mensaje igual queda en el log
// y no se queda reintentando para siempre.
@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    private final DeadLetterService service;

    public DeadLetterConsumer(DeadLetterService service) {
        this.service = service;
    }

    @RabbitListener(id = "deadLetterListener", queues = {
            RabbitNames.DLQ_CREADOS, RabbitNames.DLQ_ACEPTADOS, RabbitNames.DLQ_DESPACHADOS,
            RabbitNames.DLQ_STOCK_BAJO})
    public void onDeadLetter(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            service.registrar(message);
        } catch (Exception e) {
            log.error("No se pudo persistir el dead letter ({}): cuerpo={}",
                    e.getMessage(), new String(message.getBody()));
        }
        channel.basicAck(tag, false);
    }
}
