package abaid.pedidos360.rabbitadmin.service;

import abaid.pedidos360.rabbitadmin.dto.*;
import abaid.pedidos360.rabbitadmin.exception.BrokerNoDisponibleException;
import abaid.pedidos360.rabbitadmin.exception.OperacionNoPermitidaException;
import abaid.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;
import abaid.pedidos360.rabbitadmin.messaging.RabbitNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final RestClient managementClient;

    public RabbitAdminService(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate, RestClient rabbitManagementClient) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.managementClient = rabbitManagementClient;
    }

    // colas

    public ColaInfo crearCola(CrearColaRequest req) {
        boolean durable = req.durable() == null || req.durable();
        boolean conDlq = Boolean.TRUE.equals(req.conDlq());

        QueueBuilder builder = durable ? QueueBuilder.durable(req.nombre()) : QueueBuilder.nonDurable(req.nombre());
        if (req.ttlMs() != null) builder.ttl(req.ttlMs().intValue());
        if (req.maxLength() != null) builder.maxLength(req.maxLength());

        String dlqNombre = req.nombre() + RabbitNames.DLQ_SUFFIX;
        if (conDlq) {
            builder.deadLetterExchange(RabbitNames.DLX).deadLetterRoutingKey(dlqNombre);
        }

        conBroker(() -> {
            if (conDlq) {
                // el exchange de errores (DLX) debe existir antes de crear la cola que lo usa
                rabbitAdmin.declareExchange(ExchangeBuilder.directExchange(RabbitNames.DLX).durable(true).build());
                Queue dlq = QueueBuilder.durable(dlqNombre).build();
                rabbitAdmin.declareQueue(dlq);
                rabbitAdmin.declareBinding(new Binding(dlqNombre, Binding.DestinationType.QUEUE,
                        RabbitNames.DLX, dlqNombre, null));
            }
            rabbitAdmin.declareQueue(builder.build());
            return null;
        });
        log.info("Cola creada: {} (durable={}, dlq={})", req.nombre(), durable, conDlq);
        return infoCola(req.nombre());
    }

    public ColaInfo infoCola(String nombre) {
        var info = conBroker(() -> rabbitAdmin.getQueueInfo(nombre));
        if (info == null) {
            throw new RecursoNoEncontradoException("La cola no existe: " + nombre);
        }
        return new ColaInfo(info.getName(), "/", null, (long) info.getMessageCount(), info.getConsumerCount(), null);
    }

    public void eliminarCola(String nombre) {
        if (RabbitNames.COLAS_PROTEGIDAS.contains(nombre)) {
            throw new OperacionNoPermitidaException("La cola " + nombre + " es parte del sistema y no se puede eliminar");
        }
        infoCola(nombre); // 404 si no existe
        conBroker(() -> rabbitAdmin.deleteQueue(nombre));
        log.info("Cola eliminada: {}", nombre);
    }

    public int purgarCola(String nombre) {
        infoCola(nombre);
        Properties props = conBroker(() -> rabbitAdmin.getQueueProperties(nombre));
        int antes = props != null && props.get(RabbitAdmin.QUEUE_MESSAGE_COUNT) instanceof Number n ? n.intValue() : 0;
        conBroker(() -> { rabbitAdmin.purgeQueue(nombre, false); return null; });
        log.info("Cola purgada: {} ({} mensajes)", nombre, antes);
        return antes;
    }

    // exchanges

    public void crearExchange(CrearExchangeRequest req) {
        boolean durable = req.durable() == null || req.durable();
        ExchangeBuilder builder = switch (req.tipo()) {
            case "topic" -> ExchangeBuilder.topicExchange(req.nombre());
            case "fanout" -> ExchangeBuilder.fanoutExchange(req.nombre());
            default -> ExchangeBuilder.directExchange(req.nombre());
        };
        Exchange exchange = builder.durable(durable).build();
        conBroker(() -> { rabbitAdmin.declareExchange(exchange); return null; });
        log.info("Exchange creado: {} ({})", req.nombre(), req.tipo());
    }

    public void eliminarExchange(String nombre) {
        if (RabbitNames.EXCHANGES_PROTEGIDOS.contains(nombre)) {
            throw new OperacionNoPermitidaException("El exchange " + nombre + " es parte del sistema y no se puede eliminar");
        }
        boolean existia = conBroker(() -> rabbitAdmin.deleteExchange(nombre));
        if (!existia) {
            throw new RecursoNoEncontradoException("El exchange no existe: " + nombre);
        }
        log.info("Exchange eliminado: {}", nombre);
    }

    // bindings

    public void crearBinding(BindingRequest req) {
        infoCola(req.cola());
        Binding binding = binding(req);
        conBroker(() -> { rabbitAdmin.declareBinding(binding); return null; });
        log.info("Binding creado: {} -> {} [{}]", req.exchange(), req.cola(), req.routingKey());
    }

    public void eliminarBinding(BindingRequest req) {
        infoCola(req.cola());
        Binding binding = binding(req);
        conBroker(() -> { rabbitAdmin.removeBinding(binding); return null; });
        log.info("Binding eliminado: {} -> {} [{}]", req.exchange(), req.cola(), req.routingKey());
    }

    private Binding binding(BindingRequest req) {
        String rk = req.routingKey() == null ? "" : req.routingKey();
        return new Binding(req.cola(), Binding.DestinationType.QUEUE, req.exchange(), rk, null);
    }

    // mensajes

    public void publicar(PublicarMensajeRequest req) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        Message msg = MessageBuilder.withBody(req.payload().getBytes(StandardCharsets.UTF_8))
                .andProperties(props).build();
        conBroker(() -> { rabbitTemplate.send(req.exchange(), req.routingKey(), msg); return null; });
        log.info("Mensaje de prueba publicado en {} rk={}", req.exchange(), req.routingKey());
    }

    // listados: se piden a la API de gestion de RabbitMQ

    public List<ColaInfo> listarColas() {
        return listar("/api/queues", new ParameterizedTypeReference<List<ColaInfo>>() {})
                .stream().filter(c -> "/".equals(c.vhost())).toList();
    }

    public List<ExchangeInfo> listarExchanges() {
        return listar("/api/exchanges", new ParameterizedTypeReference<List<ExchangeInfo>>() {})
                .stream().filter(e -> "/".equals(e.vhost()) && e.name() != null && !e.name().isBlank()
                        && !e.name().startsWith("amq.")).toList();
    }

    public List<BindingInfo> listarBindings() {
        return listar("/api/bindings", new ParameterizedTypeReference<List<BindingInfo>>() {})
                .stream().filter(b -> "/".equals(b.vhost()) && b.source() != null && !b.source().isBlank()).toList();
    }

    private <T> List<T> listar(String path, ParameterizedTypeReference<List<T>> tipo) {
        try {
            List<T> res = managementClient.get().uri(path).retrieve().body(tipo);
            return res == null ? List.of() : res;
        } catch (RestClientException e) {
            throw new BrokerNoDisponibleException("No se pudo consultar la API de gestion de RabbitMQ", e);
        }
    }

    // convierte el error de conexion en un 503 claro

    private <T> T conBroker(Supplier<T> accion) {
        try {
            return accion.get();
        } catch (AmqpConnectException e) {
            throw new BrokerNoDisponibleException("RabbitMQ no esta disponible", e);
        }
    }
}
