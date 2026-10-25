# ms-pedidos360-notifications

Consume los eventos de pedidos y stock y genera notificaciones. Puerto **8083**. Servicio interno.

**Topología** (`config/RabbitMQConfig`, nombres en `messaging/RabbitNames`):

```
pedidos360.direct.exchange
  pedido.creado     -> pedidos360.pedido.creado.queue
  pedido.aceptado   -> pedidos360.pedido.aceptado.queue
  pedido.despachado -> pedidos360.pedido.despachado.queue
  stock.bajo        -> pedidos360.stock.bajo.queue
cada cola tiene x-dead-letter-exchange = pedidos360.dlx.exchange  ->  <cola>.dlq
```

**Consumidores** (`consumer/`, por dominio): `PedidoConsumer` (creado, aceptado), `DespachoConsumer`, `InventarioConsumer` (stock bajo) y `DeadLetterConsumer` (lee las DLQs).

**ACK manual** (`messaging/MensajeProcesador`):

| Caso | Qué pasa |
|---|---|
| OK | `basicAck` |
| JSON malformado o evento inválido | `basicReject` sin requeue, va a la DLQ |
| Falla al procesar | se republica con `x-attempts + 1` y ACK del original |
| 3 intentos fallidos | `basicNack` sin requeue, va a la DLQ |

Todo lo que cae en una DLQ se loguea (ERROR) y se guarda en la tabla `dead_letters`.

Para la demo: un pedido de un usuario que empiece con `falla` provoca el fallo a propósito.

| Método | Ruta | Para qué |
|---|---|---|
| GET | `/api/notifications` | todas las notificaciones |
| GET | `/api/notifications/mias?usuario=` | las de un usuario |
| GET | `/api/notifications/dead-letters` | mensajes fallidos |

**Variables:** `RABBIT_*` y, en la EC2, `SPRING_PROFILES_ACTIVE=mysql` + `MYSQL_*`.

```
mvn clean package
mvn spring-boot:run
```
