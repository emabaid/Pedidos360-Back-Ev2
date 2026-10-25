# ms-pedidos360-rabbit-admin

Administra RabbitMQ por REST. Puerto **8084**. Servicio interno: el BFF solo deja pasar al rol ADMINISTRADOR.

Toda la lógica está en `RabbitAdminService`; el controller solo valida y delega. Los DTOs llevan Bean Validation (nombre no vacío y solo `A-Z a-z 0-9 . _ -`, tipo de exchange `direct|topic|fanout`, TTL mínimo 1000 ms, etc.).

| Método | Ruta | Para qué |
|---|---|---|
| GET | `/api/admin/rabbit/queues` | listar colas |
| GET | `/api/admin/rabbit/queues/{nombre}` | mensajes y consumidores de una cola |
| POST | `/api/admin/rabbit/queues` | crear cola (`nombre`, `durable`, `conDlq`, `ttlMs`, `maxLength`) |
| DELETE | `/api/admin/rabbit/queues/{nombre}` | eliminar cola |
| DELETE | `/api/admin/rabbit/queues/{nombre}/messages` | vaciar cola |
| GET | `/api/admin/rabbit/exchanges` | listar exchanges |
| POST | `/api/admin/rabbit/exchanges` | crear exchange (`nombre`, `tipo`, `durable`) |
| DELETE | `/api/admin/rabbit/exchanges/{nombre}` | eliminar exchange |
| GET | `/api/admin/rabbit/bindings` | listar bindings |
| POST | `/api/admin/rabbit/bindings` | crear binding (`cola`, `exchange`, `routingKey`) |
| DELETE | `/api/admin/rabbit/bindings` | eliminar binding (mismo body) |
| POST | `/api/admin/rabbit/messages` | publicar mensaje de prueba |

**Códigos de error:** 400 datos inválidos (con el detalle por campo), 404 no existe, 409 operación no permitida (ej. borrar una cola del sistema) o rechazada por RabbitMQ, 503 broker caído.

Los listados usan la API de gestión de RabbitMQ (`RABBIT_MGMT_URL`, por defecto `http://localhost:15672`); crear y borrar va por AMQP con `RabbitAdmin`.

**Variables:** `RABBIT_HOST, RABBIT_PORT, RABBIT_USER, RABBIT_PASSWORD, RABBIT_MGMT_URL`.

```
mvn clean package
mvn spring-boot:run
```
