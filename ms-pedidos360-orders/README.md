# ms-pedidos360-orders

Pedidos y máquina de estados. Puerto **8081**. Servicio interno: solo lo llama el BFF.

Estados: `CREADO → ACEPTADO → EN_PREPARACION → DESPACHADO → ENTREGADO` (`CANCELADO` desde los primeros tres). Al aceptar se descuenta el stock en catalog.

| Método | Ruta | Para qué |
|---|---|---|
| GET | `/api/orders` | todos los pedidos |
| GET | `/api/orders/mios?usuario=` | pedidos de un cliente |
| GET | `/api/orders/{id}` | uno |
| POST | `/api/orders` | crear |
| PATCH | `/api/orders/{id}/estado` | cambiar estado |

**RabbitMQ:** productor. Después de guardar publica `pedido.creado`, `pedido.aceptado` o `pedido.despachado` (un `PedidoEvento`). Si el broker cae el pedido igual se guarda; solo se loguea el error.

**Variables:** `RABBIT_HOST, RABBIT_PORT, RABBIT_USER, RABBIT_PASSWORD`, y en la EC2 `SPRING_PROFILES_ACTIVE=mysql` + las `MYSQL_*`.

```
mvn clean package
mvn spring-boot:run
```
