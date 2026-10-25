package abaid.pedidos360.notifications.messaging;

import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

// Confirmacion y manejo de errores, igual para todos los consumidores:
//   mensaje roto o con datos invalidos -> se rechaza, va directo a la DLQ (reintentar no ayuda)
//   falla al procesarlo                -> se vuelve a publicar con un contador de intentos y se confirma el original
//   falla 3 veces                      -> se rechaza y va a la DLQ
//   sale bien                          -> se confirma (ACK)
@Component
public class MensajeProcesador {

    private static final Logger log = LoggerFactory.getLogger(MensajeProcesador.class);

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final RabbitTemplate rabbitTemplate;

    public MensajeProcesador(ObjectMapper objectMapper, Validator validator, RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.rabbitTemplate = rabbitTemplate;
    }

    public <T> void procesar(Message message, Channel channel, Class<T> tipo, Consumer<T> handler) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String cola = message.getMessageProperties().getConsumerQueue();

        T evento;
        try {
            evento = objectMapper.readValue(message.getBody(), tipo);
            if (evento == null) {
                throw new IllegalArgumentException("cuerpo vacio");
            }
        } catch (Exception e) {
            log.error("[{}] Mensaje malformado, va a la DLQ. Motivo: {} | cuerpo: {}",
                    cola, e.getMessage(), new String(message.getBody()));
            channel.basicReject(tag, false);
            return;
        }

        Set<ConstraintViolation<T>> errores = validator.validate(evento);
        if (!errores.isEmpty()) {
            log.error("[{}] Evento invalido, va a la DLQ. Errores: {}", cola, errores);
            channel.basicReject(tag, false);
            return;
        }

        try {
            handler.accept(evento);
            channel.basicAck(tag, false);
            log.info("[{}] ACK {}", cola, evento);
        } catch (Exception e) {
            manejarFallo(message, channel, tag, cola, evento, e);
        }
    }

    private <T> void manejarFallo(Message message, Channel channel, long tag, String cola,
            T evento, Exception error) throws IOException {
        int intentos = intentosPrevios(message) + 1;

        if (intentos >= RabbitNames.MAX_ATTEMPTS) {
            log.error("[{}] {} agoto {} intentos, NACK -> DLQ. Error: {}",
                    cola, evento, intentos, error.getMessage());
            channel.basicNack(tag, false, false);
            return;
        }

        log.warn("[{}] {} fallo (intento {}/{}), se reintenta. Error: {}",
                cola, evento, intentos, RabbitNames.MAX_ATTEMPTS, error.getMessage());
        try {
            Message reintento = MessageBuilder.fromMessage(message)
                    .setHeader(RabbitNames.HEADER_ATTEMPTS, intentos)
                    .build();
            rabbitTemplate.send(cola, reintento); // exchange por defecto -> misma cola
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("[{}] No se pudo republicar el reintento, NACK -> DLQ: {}", cola, e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }

    private int intentosPrevios(Message message) {
        Object valor = message.getMessageProperties().getHeader(RabbitNames.HEADER_ATTEMPTS);
        return valor instanceof Number n ? n.intValue() : 0;
    }
}
