package abaid.pedidos360.catalog.messaging;

import abaid.pedidos360.catalog.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class StockEventoProducer {

    private static final Logger log = LoggerFactory.getLogger(StockEventoProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public StockEventoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // si RabbitMQ no responde solo se escribe el error en el log: el stock ya se descontó igual
    public void publicarStockBajo(Product producto, int umbral) {
        StockBajoEvento evento = new StockBajoEvento(
                producto.getId(), producto.getNombre(), producto.getStock(), umbral, Instant.now());
        try {
            rabbitTemplate.convertAndSend(RabbitNames.EXCHANGE, RabbitNames.RK_STOCK_BAJO, evento);
            log.info("stock.bajo publicado producto={} stock={}", producto.getId(), producto.getStock());
        } catch (Exception e) {
            log.error("No se pudo publicar stock.bajo producto={}: {}", producto.getId(), e.getMessage());
        }
    }
}
