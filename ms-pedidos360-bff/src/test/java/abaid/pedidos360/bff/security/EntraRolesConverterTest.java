package abaid.pedidos360.bff.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntraRolesConverterTest {

    private final EntraRolesConverter converter = new EntraRolesConverter();

    private Jwt jwt(Map<String, Object> claims) {
        Jwt.Builder b = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("usuario-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60));
        claims.forEach(b::claim);
        return b.build();
    }

    @Test
    void usaElClaimRolesTalCual() {
        var auth = converter.convert(jwt(Map.of("roles", List.of("ROLE_ADMINISTRADOR"))));
        List<String> nombres = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertEquals(List.of("ROLE_ADMINISTRADOR"), nombres);
    }

    @Test
    void sinClaimRoles_quedaSinAuthorities() {
        var auth = converter.convert(jwt(Map.of("scp", "Pedidos.Read")));
        assertTrue(auth.getAuthorities().isEmpty());
    }
}
