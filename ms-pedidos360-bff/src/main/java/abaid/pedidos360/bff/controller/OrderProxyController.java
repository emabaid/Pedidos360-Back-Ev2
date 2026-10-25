package abaid.pedidos360.bff.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

// Reenvia a orders. La seguridad no depende de que el front llame a la ruta "correcta":
// GET /api/orders mira el rol del token y trae todos los pedidos o solo los del cliente,
// y GET /{id} comprueba que el pedido sea del usuario antes de devolverlo.
@RestController
@RequestMapping("/api/orders")
public class OrderProxyController {

    private final RestClient ordersRestClient;

    public OrderProxyController(RestClient ordersRestClient) {
        this.ordersRestClient = ordersRestClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public List<Map<String, Object>> listar(Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        if (esGestor(authentication)) {
            // administrador u operador: ven todos los pedidos
            return ordersRestClient.get()
                    .uri("/api/orders")
                    .retrieve()
                    .body(List.class);
        }

        // cliente: el usuario a filtrar sale del token, nunca de un dato que mande el navegador
        String usuario = usuarioDesde(jwt);
        return ordersRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/orders/mios")
                        .queryParam("usuario", usuario)
                        .build())
                .retrieve()
                .body(List.class);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public Map<String, Object> obtener(@PathVariable Long id, Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> pedido = ordersRestClient.get()
                .uri("/api/orders/{id}", id)
                .retrieve()
                .body(Map.class);

        if (!esGestor(authentication)) {
            String usuario = usuarioDesde(jwt);
            Object dueño = pedido != null ? pedido.get("clienteUsername") : null;

            if (!usuario.equals(dueño)) {
                // el mensaje no revela si el pedido existe, solo que no es suyo
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este pedido no te pertenece");
            }
        }

        return pedido;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public Map<String, Object> crear(@RequestBody Map<String, Object> pedido, @AuthenticationPrincipal Jwt jwt) {
        // el pedido queda siempre a nombre del usuario del token, aunque el body traiga otro
        pedido.put("clienteUsername", usuarioDesde(jwt));

        return ordersRestClient.post()
                .uri("/api/orders")
                .body(pedido)
                .retrieve()
                .body(Map.class);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('OPERADOR')")
    public Map<String, Object> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ordersRestClient.patch()
                .uri("/api/orders/{id}/estado", id)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private boolean esGestor(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String rol = authority.getAuthority();
            if (rol.equals("ROLE_ADMINISTRADOR") || rol.equals("ROLE_OPERADOR")) {
                return true;
            }
        }
        return false;
    }

    // Identifica al usuario con datos del token. "email" y "preferred_username" solo vienen
    // si se activan en Entra (optional claims); "sub" siempre viene, por eso va de ultimo recurso.
    private String usuarioDesde(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null) return email;

        String upn = jwt.getClaimAsString("preferred_username");
        if (upn != null) return upn;

        return jwt.getSubject();
    }
}
