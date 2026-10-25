package abaid.pedidos360.notifications.service;

import abaid.pedidos360.notifications.messaging.RabbitNames;
import abaid.pedidos360.notifications.model.DeadLetterRegistro;
import abaid.pedidos360.notifications.repository.DeadLetterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class DeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterService.class);

    private final DeadLetterRepository repository;

    public DeadLetterService(DeadLetterRepository repository) {
        this.repository = repository;
    }

    public void registrar(Message message) {
        String cola = "desconocida";
        String motivo = "desconocido";
        List<Map<String, ?>> xDeath = message.getMessageProperties().getXDeathHeader();
        if (xDeath != null && !xDeath.isEmpty()) {
            Map<String, ?> primero = xDeath.get(0);
            cola = String.valueOf(primero.get("queue"));
            motivo = String.valueOf(primero.get("reason"));
        }
        Object h = message.getMessageProperties().getHeader(RabbitNames.HEADER_ATTEMPTS);
        int intentos = h instanceof Number n ? n.intValue() + 1 : 1;

        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        if (payload.length() > 4000) {
            payload = payload.substring(0, 4000);
        }

        log.error("DEAD LETTER cola={} motivo={} intentos={} payload={}", cola, motivo, intentos, payload);
        repository.save(new DeadLetterRegistro(cola, motivo, intentos, payload));
    }

    public List<DeadLetterRegistro> listar() {
        return repository.findAllByOrderByFechaDesc();
    }
}
