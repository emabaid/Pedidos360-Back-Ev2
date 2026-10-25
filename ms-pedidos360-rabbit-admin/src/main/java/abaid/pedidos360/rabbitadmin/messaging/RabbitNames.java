package abaid.pedidos360.rabbitadmin.messaging;

import java.util.Set;

// Colas y exchanges "protegidos": son los que usa el sistema y la API no deja eliminarlos.
public final class RabbitNames {

    private RabbitNames() {}

    public static final String EXCHANGE = "pedidos360.direct.exchange";
    public static final String DLX = "pedidos360.dlx.exchange";

    public static final String Q_CREADOS = "pedidos360.pedido.creado.queue";
    public static final String Q_ACEPTADOS = "pedidos360.pedido.aceptado.queue";
    public static final String Q_DESPACHADOS = "pedidos360.pedido.despachado.queue";
    public static final String DLQ_CREADOS = "pedidos360.pedido.creado.dlq";
    public static final String DLQ_ACEPTADOS = "pedidos360.pedido.aceptado.dlq";
    public static final String DLQ_DESPACHADOS = "pedidos360.pedido.despachado.dlq";

    public static final String Q_STOCK_BAJO = "pedidos360.stock.bajo.queue";
    public static final String DLQ_STOCK_BAJO = "pedidos360.stock.bajo.dlq";

    public static final String DLQ_SUFFIX = ".dlq";

    public static final Set<String> COLAS_PROTEGIDAS = Set.of(
            Q_CREADOS, Q_ACEPTADOS, Q_DESPACHADOS, Q_STOCK_BAJO,
            DLQ_CREADOS, DLQ_ACEPTADOS, DLQ_DESPACHADOS, DLQ_STOCK_BAJO);

    public static final Set<String> EXCHANGES_PROTEGIDOS = Set.of(EXCHANGE, DLX);
}
