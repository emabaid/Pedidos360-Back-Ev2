package abaid.pedidos360.bff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

// Reenvia a notifications. Igual que /api/orders: hay una sola ruta y es el token
// (no el navegador) quien decide si ves todo o solo lo tuyo.
@RestController
@RequestMapping("/api/notifications")
public class NotificationsProxyController {

    private final RestClient notificationsRestClient;

    public NotificationsProxyController(RestClient notificationsRestClient) {
        this.notificationsRestClient = notificationsRestClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public List<Map<String, Object>> listar(Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        if (esGestor(authentication)) {
            return notificationsRestClient.get().uri("/api/notifications").retrieve().body(List.class);
        }
        String usuario = usuarioDesde(jwt);
        return notificationsRestClient.get()
                .uri(b -> b.path("/api/notifications/mias").queryParam("usuario", usuario).build())
                .retrieve()
                .body(List.class);
    }

    // mensajes que fallaron y quedaron en una DLQ (cola de mensajes muertos)
    @GetMapping("/dead-letters")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Map<String, Object>> deadLetters() {
        return notificationsRestClient.get().uri("/api/notifications/dead-letters").retrieve().body(List.class);
    }

    private boolean esGestor(Authentication authentication) {
        for (GrantedAuthority a : authentication.getAuthorities()) {
            String rol = a.getAuthority();
            if (rol.equals("ROLE_ADMINISTRADOR") || rol.equals("ROLE_OPERADOR")) return true;
        }
        return false;
    }

    private String usuarioDesde(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null) return email;
        String upn = jwt.getClaimAsString("preferred_username");
        if (upn != null) return upn;
        return jwt.getSubject();
    }
}
