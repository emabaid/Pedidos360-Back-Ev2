package abaid.pedidos360.bff.controller;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.HttpServletRequest;

// Reenvia tal cual cualquier peticion /api/admin/rabbit/** a rabbit-admin. Solo ADMINISTRADOR.
// Devuelve el mismo codigo HTTP que responda rabbit-admin (404, 409, 503...).
@RestController
@RequestMapping("/api/admin/rabbit")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class RabbitAdminProxyController {

    private final RestClient rabbitAdminRestClient;

    public RabbitAdminProxyController(RestClient rabbitAdminRestClient) {
        this.rabbitAdminRestClient = rabbitAdminRestClient;
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> reenviar(HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {
        String destino = request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        RestClient.RequestBodySpec spec = rabbitAdminRestClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(destino)
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null && body.length > 0) {
            spec.body(body);
        }

        // sin esta linea, un 404/409 de rabbit-admin se convertiria en excepcion y se
        // perderia el codigo
        ResponseEntity<byte[]> respuesta = spec.retrieve()
                .onStatus(status -> true, (req, res) -> {
                })
                .toEntity(byte[].class);

        // devolvemos solo codigo y cuerpo; copiar las cabeceras (ej. Content-Length)
        // puede dar problemas
        return ResponseEntity.status(respuesta.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(respuesta.getBody());
    }
}
