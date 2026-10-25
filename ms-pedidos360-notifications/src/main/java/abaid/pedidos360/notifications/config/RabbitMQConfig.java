package abaid.pedidos360.notifications.config;

import abaid.pedidos360.notifications.messaging.RabbitNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Aqui se define todo lo de RabbitMQ: exchanges, colas, colas de errores (DLQ) y bindings.
// No hay logica de negocio en esta clase.
@Configuration
public class RabbitMQConfig {

    // exchanges
    @Bean
    public DirectExchange pedidosExchange() {
        return ExchangeBuilder.directExchange(RabbitNames.EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(RabbitNames.DLX).durable(true).build();
    }

    // colas principales; cada una manda sus mensajes fallidos a su DLQ via el exchange DLX
    @Bean
    public Queue colaCreados() {
        return conDlq(RabbitNames.Q_CREADOS, RabbitNames.RK_DLQ_CREADO);
    }

    @Bean
    public Queue colaAceptados() {
        return conDlq(RabbitNames.Q_ACEPTADOS, RabbitNames.RK_DLQ_ACEPTADO);
    }

    @Bean
    public Queue colaDespachados() {
        return conDlq(RabbitNames.Q_DESPACHADOS, RabbitNames.RK_DLQ_DESPACHADO);
    }

    @Bean
    public Queue colaStockBajo() {
        return conDlq(RabbitNames.Q_STOCK_BAJO, RabbitNames.RK_DLQ_STOCK_BAJO);
    }

    // DLQ = cola donde terminan los mensajes que fallaron
    @Bean
    public Queue dlqCreados() {
        return QueueBuilder.durable(RabbitNames.DLQ_CREADOS).build();
    }

    @Bean
    public Queue dlqAceptados() {
        return QueueBuilder.durable(RabbitNames.DLQ_ACEPTADOS).build();
    }

    @Bean
    public Queue dlqDespachados() {
        return QueueBuilder.durable(RabbitNames.DLQ_DESPACHADOS).build();
    }

    @Bean
    public Queue dlqStockBajo() {
        return QueueBuilder.durable(RabbitNames.DLQ_STOCK_BAJO).build();
    }

    // bindings: conectan cada cola con su exchange segun la routing key
    @Bean
    public Binding bindingCreados() {
        return BindingBuilder.bind(colaCreados()).to(pedidosExchange()).with(RabbitNames.RK_CREADO);
    }

    @Bean
    public Binding bindingAceptados() {
        return BindingBuilder.bind(colaAceptados()).to(pedidosExchange()).with(RabbitNames.RK_ACEPTADO);
    }

    @Bean
    public Binding bindingDespachados() {
        return BindingBuilder.bind(colaDespachados()).to(pedidosExchange()).with(RabbitNames.RK_DESPACHADO);
    }

    @Bean
    public Binding bindingStockBajo() {
        return BindingBuilder.bind(colaStockBajo()).to(pedidosExchange()).with(RabbitNames.RK_STOCK_BAJO);
    }

    @Bean
    public Binding bindingDlqStockBajo() {
        return BindingBuilder.bind(dlqStockBajo()).to(deadLetterExchange()).with(RabbitNames.RK_DLQ_STOCK_BAJO);
    }

    @Bean
    public Binding bindingDlqCreados() {
        return BindingBuilder.bind(dlqCreados()).to(deadLetterExchange()).with(RabbitNames.RK_DLQ_CREADO);
    }

    @Bean
    public Binding bindingDlqAceptados() {
        return BindingBuilder.bind(dlqAceptados()).to(deadLetterExchange()).with(RabbitNames.RK_DLQ_ACEPTADO);
    }

    @Bean
    public Binding bindingDlqDespachados() {
        return BindingBuilder.bind(dlqDespachados()).to(deadLetterExchange()).with(RabbitNames.RK_DLQ_DESPACHADO);
    }

    // utilidades de Spring AMQP (admin, conversor JSON, template y listeners)
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    // el consumidor confirma (ACK) a mano, recibe un mensaje a la vez y si falla no se reencola:
    // el mensaje va a la DLQ
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setDefaultRequeueRejected(false);
        factory.setMissingQueuesFatal(false);
        return factory;
    }

    private Queue conDlq(String nombre, String dlqRoutingKey) {
        return QueueBuilder.durable(nombre)
                .withArgument("x-dead-letter-exchange", RabbitNames.DLX)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }
}
