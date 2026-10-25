package abaid.pedidos360.notifications.service;

import abaid.pedidos360.notifications.dto.PedidoEvento;
import abaid.pedidos360.notifications.dto.StockBajoEvento;
import abaid.pedidos360.notifications.model.Notificacion;
import abaid.pedidos360.notifications.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    // solo para la demo: si el usuario empieza con "falla", este servicio falla a proposito
    // y asi se ven los reintentos y la DLQ
    static final String PREFIJO_FALLA_SIMULADA = "falla";

    private final NotificacionRepository repository;

    public NotificacionService(NotificacionRepository repository) {
        this.repository = repository;
    }

    public void notificarCreado(PedidoEvento e) {
        guardar(e, "PEDIDO_CREADO", "Recibimos tu pedido #" + e.pedidoId() + ". Esta pendiente de aceptacion.");
    }

    public void notificarAceptado(PedidoEvento e) {
        guardar(e, "PEDIDO_ACEPTADO", "Tu pedido #" + e.pedidoId() + " fue aceptado y esta en proceso.");
    }

    public void notificarDespachado(PedidoEvento e) {
        guardar(e, "PEDIDO_DESPACHADO", "Tu pedido #" + e.pedidoId() + " ya fue despachado.");
    }

    public void notificarStockBajo(StockBajoEvento e) {
        repository.save(new Notificacion(null, "STOCK_BAJO", "inventario",
                "Stock bajo: " + e.nombre() + " (quedan " + e.stock() + ")"));
        log.warn("Stock bajo de {} (quedan {})", e.nombre(), e.stock());
    }

    public List<Notificacion> listarTodas() {
        return repository.findAllByOrderByFechaDesc();
    }

    public List<Notificacion> listarPorUsuario(String usuario) {
        return repository.findByDestinatarioOrderByFechaDesc(usuario);
    }

    private void guardar(PedidoEvento e, String tipo, String mensaje) {
        if (e.clienteUsername().toLowerCase().startsWith(PREFIJO_FALLA_SIMULADA)) {
            throw new IllegalStateException("Fallo simulado de notificacion para " + e.clienteUsername());
        }
        repository.save(new Notificacion(e.pedidoId(), tipo, e.clienteUsername(), mensaje));
        log.info("Notificacion {} para {}: {}", tipo, e.clienteUsername(), mensaje);
    }
}
