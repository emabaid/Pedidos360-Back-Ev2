package abaid.pedidos360.rabbitadmin.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// Convierte cada error en el codigo HTTP que corresponde (400, 404, 409, 503),
// en vez de responder 200 con un mensaje de error adentro.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<Map<String, Object>> noPermitida(OperacionNoPermitidaException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BrokerNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> brokerCaido(BrokerNoDisponibleException e) {
        log.error("Broker no disponible: {}", e.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("status", 400);
        cuerpo.put("error", "Datos invalidos");
        cuerpo.put("campos", campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(org.springframework.amqp.AmqpConnectException.class)
    public ResponseEntity<Map<String, Object>> sinConexion(org.springframework.amqp.AmqpConnectException e) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no esta disponible");
    }

    // errores que devuelve RabbitMQ, ej: intentar crear una cola que ya existe con otra configuracion
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> amqp(AmqpException e) {
        log.error("Error AMQP: {}", e.getMessage());
        return respuesta(HttpStatus.CONFLICT, "RabbitMQ rechazo la operacion: " + e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("status", status.value());
        cuerpo.put("error", mensaje);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
