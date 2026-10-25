package abaid.pedidos360.orders.service;

import abaid.pedidos360.orders.client.CatalogClient;
import abaid.pedidos360.orders.client.ProductoInfo;
import abaid.pedidos360.orders.dto.CrearPedidoRequest;
import abaid.pedidos360.orders.dto.ItemPedidoRequest;
import abaid.pedidos360.orders.model.EstadoPedido;
import abaid.pedidos360.orders.model.Order;
import abaid.pedidos360.orders.model.OrderItem;
import abaid.pedidos360.orders.repository.OrderRepository;
import abaid.pedidos360.orders.messaging.PedidoEventoProducer;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final CatalogClient catalogClient;
    private final PedidoEventoProducer eventoProducer;

    // Flujo: CREADO -> ACEPTADO -> EN_PREPARACION -> DESPACHADO -> ENTREGADO.
    // Se puede CANCELAR hasta antes de despachar.
    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = new EnumMap<>(EstadoPedido.class);
    static {
        TRANSICIONES.put(EstadoPedido.CREADO, EnumSet.of(EstadoPedido.ACEPTADO, EstadoPedido.CANCELADO));
        TRANSICIONES.put(EstadoPedido.ACEPTADO, EnumSet.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO));
        TRANSICIONES.put(EstadoPedido.EN_PREPARACION, EnumSet.of(EstadoPedido.DESPACHADO, EstadoPedido.CANCELADO));
        TRANSICIONES.put(EstadoPedido.DESPACHADO, EnumSet.of(EstadoPedido.ENTREGADO));
        TRANSICIONES.put(EstadoPedido.ENTREGADO, EnumSet.noneOf(EstadoPedido.class));
        TRANSICIONES.put(EstadoPedido.CANCELADO, EnumSet.noneOf(EstadoPedido.class));
    }

    public OrderService(OrderRepository repository, CatalogClient catalogClient, PedidoEventoProducer eventoProducer) {
        this.repository = repository;
        this.catalogClient = catalogClient;
        this.eventoProducer = eventoProducer;
    }

    public List<Order> listarTodos() {
        return repository.findAll();
    }

    public List<Order> listarPorCliente(String clienteUsername) {
        return repository.findByClienteUsername(clienteUsername);
    }

    public Order obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + id));
    }

    public Order crear(CrearPedidoRequest request) {
        Order order = new Order();
        order.setClienteUsername(request.getClienteUsername());
        order.setEstado(EstadoPedido.CREADO);

        for (ItemPedidoRequest itemReq : request.getItems()) {
            ProductoInfo producto = catalogClient.obtenerProducto(itemReq.getProductoId());

            OrderItem item = new OrderItem();
            item.setProductoId(producto.getId());
            item.setProductoNombre(producto.getNombre());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(producto.getPrecio());
            item.setOrder(order);
            order.getItems().add(item);
        }

        Order guardado = repository.save(order);
        eventoProducer.publicar(guardado);
        return guardado;
    }

    // Valida que el cambio de estado sea permitido (ej: no se puede despachar sin aceptar).
    // Al aceptar se descuenta el stock en catalog. Despues publica el evento en RabbitMQ.
    public Order cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Order order = obtener(id);
        EstadoPedido actual = order.getEstado();

        Set<EstadoPedido> permitidos = TRANSICIONES.getOrDefault(actual, EnumSet.noneOf(EstadoPedido.class));
        if (!permitidos.contains(nuevoEstado)) {
            throw new IllegalStateException(
                "Transicion invalida: no se puede pasar de " + actual + " a " + nuevoEstado
                + " (ej.: no se puede DESPACHAR sin ACEPTAR)"
            );
        }

        if (nuevoEstado == EstadoPedido.ACEPTADO) {
            for (OrderItem item : order.getItems()) {
                catalogClient.disminuirStock(item.getProductoId(), item.getCantidad());
            }
        }

        order.setEstado(nuevoEstado);
        Order guardado = repository.save(order);
        eventoProducer.publicar(guardado);
        return guardado;
    }
}
