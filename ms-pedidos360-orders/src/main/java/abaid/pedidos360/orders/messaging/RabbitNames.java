package abaid.pedidos360.orders.messaging;

public final class RabbitNames {

    private RabbitNames() {}

    public static final String EXCHANGE = "pedidos360.direct.exchange";
    public static final String DLX = "pedidos360.dlx.exchange";

    public static final String RK_PEDIDO_CREADO = "pedido.creado";
    public static final String RK_PEDIDO_ACEPTADO = "pedido.aceptado";
    public static final String RK_PEDIDO_DESPACHADO = "pedido.despachado";
}
