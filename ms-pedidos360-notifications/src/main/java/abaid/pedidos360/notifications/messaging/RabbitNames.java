package abaid.pedidos360.notifications.messaging;

public final class RabbitNames {

    private RabbitNames() {}

    public static final String EXCHANGE = "pedidos360.direct.exchange";
    public static final String DLX = "pedidos360.dlx.exchange";

    // colas principales
    public static final String Q_CREADOS = "pedidos360.pedido.creado.queue";
    public static final String Q_ACEPTADOS = "pedidos360.pedido.aceptado.queue";
    public static final String Q_DESPACHADOS = "pedidos360.pedido.despachado.queue";

    public static final String Q_STOCK_BAJO = "pedidos360.stock.bajo.queue";

    // colas de mensajes fallidos (DLQ)
    public static final String DLQ_CREADOS = "pedidos360.pedido.creado.dlq";
    public static final String DLQ_ACEPTADOS = "pedidos360.pedido.aceptado.dlq";
    public static final String DLQ_DESPACHADOS = "pedidos360.pedido.despachado.dlq";

    public static final String DLQ_STOCK_BAJO = "pedidos360.stock.bajo.dlq";

    // Routing keys
    public static final String RK_CREADO = "pedido.creado";
    public static final String RK_ACEPTADO = "pedido.aceptado";
    public static final String RK_DESPACHADO = "pedido.despachado";

    public static final String RK_STOCK_BAJO = "stock.bajo";
    public static final String RK_DLQ_STOCK_BAJO = "stock.bajo.dlq";

    public static final String RK_DLQ_CREADO = "pedido.creado.dlq";
    public static final String RK_DLQ_ACEPTADO = "pedido.aceptado.dlq";
    public static final String RK_DLQ_DESPACHADO = "pedido.despachado.dlq";

    // header con el numero de intentos y maximo de intentos antes de ir a la DLQ
    public static final String HEADER_ATTEMPTS = "x-attempts";
    public static final int MAX_ATTEMPTS = 3;
}
