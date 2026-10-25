package abaid.pedidos360.notifications.messaging;

import abaid.pedidos360.notifications.dto.PedidoEvento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MensajeProcesadorTest {

    private static final String COLA = "cola.test";

    private Channel channel;
    private RabbitTemplate template;
    private MensajeProcesador procesador;

    @BeforeEach
    void setUp() {
        channel = mock(Channel.class);
        template = mock(RabbitTemplate.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        procesador = new MensajeProcesador(mapper, validator, template);
    }

    private Message mensaje(String json, Integer intentos) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(7L);
        props.setConsumerQueue(COLA);
        if (intentos != null) props.setHeader(RabbitNames.HEADER_ATTEMPTS, intentos);
        return new Message(json.getBytes(), props);
    }

    private static final String VALIDO =
            "{\"pedidoId\":1,\"estado\":\"CREADO\",\"clienteUsername\":\"ana@x.cl\",\"total\":10.0}";

    @Test
    void mensajeValido_haceAck() throws Exception {
        procesador.procesar(mensaje(VALIDO, null), channel, PedidoEvento.class, e -> {});
        verify(channel).basicAck(7L, false);
    }

    @Test
    void mensajeMalformado_haceRejectSinRequeue() throws Exception {
        procesador.procesar(mensaje("no-es-json", null), channel, PedidoEvento.class, e -> {});
        verify(channel).basicReject(7L, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void eventoInvalido_haceRejectSinRequeue() throws Exception {
        procesador.procesar(mensaje("{\"pedidoId\":null,\"estado\":\"\"}", null), channel, PedidoEvento.class, e -> {});
        verify(channel).basicReject(7L, false);
    }

    @Test
    void falloTransitorio_republicaYHaceAck() throws Exception {
        procesador.procesar(mensaje(VALIDO, 0), channel, PedidoEvento.class, (PedidoEvento e) -> { throw new IllegalStateException("x"); });
        verify(template).send(eq(COLA), any(Message.class));
        verify(channel).basicAck(7L, false);
    }

    @Test
    void reintentosAgotados_haceNackSinRequeue() throws Exception {
        procesador.procesar(mensaje(VALIDO, RabbitNames.MAX_ATTEMPTS - 1), channel, PedidoEvento.class,
                (PedidoEvento e) -> { throw new IllegalStateException("x"); });
        verify(channel).basicNack(7L, false, false);
        verify(template, never()).send(any(String.class), any(Message.class));
    }
}
