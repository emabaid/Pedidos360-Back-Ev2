# Pedidos360 – backend

Cinco microservicios Spring Boot 3.3 (Java 17, Maven), cada uno con su propio README.

| Servicio | Puerto | Rol |
|---|---|---|
| ms-pedidos360-bff | 8080 | entrada del frontend, valida JWT y roles |
| ms-pedidos360-orders | 8081 | pedidos y estados. **Productor** de eventos |
| ms-pedidos360-catalog | 8082 | productos y stock. **Productor** de `stock.bajo` |
| ms-pedidos360-notifications | 8083 | **Consumidores** (ACK manual, reintentos, DLQ) |
| ms-pedidos360-rabbit-admin | 8084 | administra colas, exchanges y bindings por REST |

```
frontend --JWT--> API Gateway --> BFF --> orders / catalog / notifications / rabbit-admin
                                            |         |
                                            +---------+--> RabbitMQ (pedidos360.direct.exchange) --> notifications
```

## RabbitMQ

- Exchange `pedidos360.direct.exchange`; routing keys `pedido.creado`, `pedido.aceptado`, `pedido.despachado`, `stock.bajo`.
- Cada cola tiene su DLQ vía `pedidos360.dlx.exchange`.
- Errores: malformado → DLQ directo; falla al procesar → 3 intentos → DLQ. Todo lo que llega a una DLQ se loguea y se guarda en BD (detalle en el README de notifications).
- Si RabbitMQ cae, orders y catalog siguen funcionando (solo loguean el error).

## Correr local

1. Levantar RabbitMQ (Docker Desktop):
   ```
   Copiar .env.example como .env
   docker compose up -d     # panel en http://localhost:15672
   ```
   Con `RABBIT_USER=guest` y `RABBIT_PASSWORD=guest` en el `.env` los servicios conectan sin configurar nada. Si usas otras credenciales, exportarlas en cada terminal antes de arrancar.
2. En cada carpeta: `mvn clean package` y luego `mvn spring-boot:run`.
3. Por defecto catalog, orders y notifications usan H2 en archivo (`./data`). Antes de correr el BFF completar `issuer-uri` y `audience` en su `application.yml`.

## Base de datos en la EC2

Perfil `mysql` (`SPRING_PROFILES_ACTIVE=mysql`) con `MYSQL_HOST, MYSQL_PORT, MYSQL_DB, MYSQL_USER, MYSQL_PASSWORD`. Los tres servicios con BD comparten la misma base; sus tablas no chocan. Con MySQL el `data.sql` no corre, así que los productos de ejemplo se insertan a mano una vez:

```sql
INSERT INTO PRODUCTS (nombre, descripcion, precio, stock) VALUES
  ('Notebook 14"', 'Notebook basico para oficina', 450000.0, 15),
  ('Mouse inalambrico', 'Mouse optico USB', 8990.0, 50),
  ('Teclado mecanico', 'Teclado mecanico switch rojo', 34990.0, 20);
```

## Probar el flujo de mensajes

Orders no pide token (es interno), se le puede llamar directo:

```powershell
$body = '{"clienteUsername":"ana@test.cl","items":[{"productoId":1,"cantidad":2}]}'
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/orders -ContentType "application/json" -Body $body
```

Un `clienteUsername` que empiece con `falla` fuerza el fallo y termina en la DLQ.
