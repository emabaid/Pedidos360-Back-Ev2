package abaid.pedidos360.orders.service;

import abaid.pedidos360.orders.client.CatalogClient;
import abaid.pedidos360.orders.client.ProductoInfo;
import abaid.pedidos360.orders.dto.CrearPedidoRequest;
import abaid.pedidos360.orders.dto.ItemPedidoRequest;
import abaid.pedidos360.orders.messaging.PedidoEventoProducer;
import abaid.pedidos360.orders.model.EstadoPedido;
import abaid.pedidos360.orders.model.Order;
import abaid.pedidos360.orders.model.OrderItem;
import abaid.pedidos360.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository repository;
    private CatalogClient catalogClient;
    private PedidoEventoProducer producer;
    private OrderService service;

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        catalogClient = mock(CatalogClient.class);
        producer = mock(PedidoEventoProducer.class);
        service = new OrderService(repository, catalogClient, producer);
        when(repository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Order pedidoEn(EstadoPedido estado) {
        Order o = new Order();
        o.setClienteUsername("ana@test.cl");
        o.setEstado(estado);
        OrderItem item = new OrderItem();
        item.setProductoId(1L);
        item.setCantidad(2);
        item.setOrder(o);
        o.getItems().add(item);
        when(repository.findById(10L)).thenReturn(Optional.of(o));
        return o;
    }

    @Test
    void crear_guardaComoCreadoYPublicaEvento() {
        ProductoInfo producto = new ProductoInfo();
        producto.setId(1L);
        producto.setNombre("Mouse");
        producto.setPrecio(1000.0);
        when(catalogClient.obtenerProducto(1L)).thenReturn(producto);

        ItemPedidoRequest item = new ItemPedidoRequest();
        item.setProductoId(1L);
        item.setCantidad(2);
        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setClienteUsername("ana@test.cl");
        req.setItems(List.of(item));

        Order creado = service.crear(req);

        assertEquals(EstadoPedido.CREADO, creado.getEstado());
        verify(producer).publicar(creado);
    }

    @Test
    void aceptar_descuentaStockYPublicaEvento() {
        pedidoEn(EstadoPedido.CREADO);
        Order resultado = service.cambiarEstado(10L, EstadoPedido.ACEPTADO);

        assertEquals(EstadoPedido.ACEPTADO, resultado.getEstado());
        verify(catalogClient).disminuirStock(1L, 2);
        verify(producer).publicar(resultado);
    }

    @Test
    void despacharSinAceptar_lanzaErrorYNoPublica() {
        pedidoEn(EstadoPedido.CREADO);
        assertThrows(IllegalStateException.class, () -> service.cambiarEstado(10L, EstadoPedido.DESPACHADO));
        verify(producer, never()).publicar(any());
    }
}
