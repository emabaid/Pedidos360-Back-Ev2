package abaid.pedidos360.notifications.repository;

import abaid.pedidos360.notifications.model.DeadLetterRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeadLetterRepository extends JpaRepository<DeadLetterRegistro, Long> {
    List<DeadLetterRegistro> findAllByOrderByFechaDesc();
}
