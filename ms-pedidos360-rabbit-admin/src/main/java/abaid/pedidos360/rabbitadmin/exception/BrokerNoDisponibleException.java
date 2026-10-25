package abaid.pedidos360.rabbitadmin.exception;

public class BrokerNoDisponibleException extends RuntimeException {
    public BrokerNoDisponibleException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
