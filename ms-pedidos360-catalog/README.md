# ms-pedidos360-catalog

Productos, precios y stock. Puerto **8082**. Servicio interno: solo lo llama el BFF.

| Método | Ruta | Para qué |
|---|---|---|
| GET | `/api/catalog` | listar productos |
| GET | `/api/catalog/{id}` | ver uno |
| POST / PUT / DELETE | `/api/catalog[/{id}]` | crear, editar, borrar |
| PATCH | `/api/catalog/{id}/stock/disminuir` y `/aumentar` | ajustar stock |

**RabbitMQ:** productor. Cuando el stock queda en el umbral o menos (`pedidos360.stock.umbral-bajo`, 5 por defecto) publica `stock.bajo` en `pedidos360.direct.exchange`. Si el broker está caído solo se loguea.

**Base de datos:** H2 en archivo por defecto. En la EC2 `SPRING_PROFILES_ACTIVE=mysql` + `MYSQL_HOST, MYSQL_PORT, MYSQL_DB, MYSQL_USER, MYSQL_PASSWORD`.

**RabbitMQ (vars):** `RABBIT_HOST, RABBIT_PORT, RABBIT_USER, RABBIT_PASSWORD` (por defecto localhost, guest/guest).

```
mvn clean package
mvn spring-boot:run
```
