package abaid.pedidos360.rabbitadmin.service;

import abaid.pedidos360.rabbitadmin.exception.BrokerNoDisponibleException;
import abaid.pedidos360.rabbitadmin.exception.OperacionNoPermitidaException;
import abaid.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;
import abaid.pedidos360.rabbitadmin.messaging.RabbitNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RabbitAdminServiceTest {

    private RabbitAdmin admin;
    private RabbitAdminService service;

    @BeforeEach
    void setUp() {
        admin = mock(RabbitAdmin.class);
        service = new RabbitAdminService(admin, mock(RabbitTemplate.class), mock(RestClient.class));
    }

    @Test
    void noPermiteEliminarColaProtegida() {
        assertThrows(OperacionNoPermitidaException.class, () -> service.eliminarCola(RabbitNames.Q_CREADOS));
        verify(admin, never()).deleteQueue(anyString());
    }

    @Test
    void noPermiteEliminarExchangeProtegido() {
        assertThrows(OperacionNoPermitidaException.class, () -> service.eliminarExchange(RabbitNames.EXCHANGE));
    }

    @Test
    void colaInexistente_lanza404() {
        when(admin.getQueueInfo("no-existe")).thenReturn(null);
        assertThrows(RecursoNoEncontradoException.class, () -> service.infoCola("no-existe"));
    }

    @Test
    void brokerCaido_lanzaBrokerNoDisponible() {
        when(admin.getQueueInfo("x")).thenThrow(new AmqpConnectException(new RuntimeException("down")));
        assertThrows(BrokerNoDisponibleException.class, () -> service.infoCola("x"));
    }
}
