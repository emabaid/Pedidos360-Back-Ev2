# ms-pedidos360-bff

Única puerta de entrada del frontend. Puerto **8080**. Valida el JWT de Entra ID (firma, issuer, vigencia y audience) y decide por el rol del token antes de reenviar.

| Ruta | Roles | Destino |
|---|---|---|
| `GET /api/publico` | todos | health simple |
| `/api/catalog/**` | ver: todos · editar: OPERADOR | catalog |
| `/api/orders/**` | todos (el cliente solo ve lo suyo) · cambiar estado: OPERADOR | orders |
| `GET /api/notifications` | todos (el cliente solo ve lo suyo) | notifications |
| `GET /api/notifications/dead-letters` | ADMINISTRADOR | notifications |
| `/api/admin/rabbit/**` | ADMINISTRADOR | rabbit-admin |

El filtro por usuario sale siempre del token (email, preferred_username o sub), nunca de un parámetro del navegador.

**Config** (`application.yml`): `pedidos360.entra.issuer-uri`, `pedidos360.entra.audience`, `pedidos360.cors.allowed-origins` y las URLs de los servicios internos.

```
mvn clean package
mvn spring-boot:run
```
