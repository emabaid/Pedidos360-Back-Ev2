package abaid.pedidos360.catalog.messaging;

import java.time.Instant;

public record StockBajoEvento(Long productoId, String nombre, Integer stock, Integer umbral, Instant timestamp) {}
