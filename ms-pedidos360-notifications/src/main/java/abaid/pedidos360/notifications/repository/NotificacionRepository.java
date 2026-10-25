package abaid.pedidos360.notifications.repository;

import abaid.pedidos360.notifications.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByDestinatarioOrderByFechaDesc(String destinatario);
    List<Notificacion> findAllByOrderByFechaDesc();
}
