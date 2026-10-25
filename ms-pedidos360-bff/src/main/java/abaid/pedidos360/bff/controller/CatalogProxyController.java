package abaid.pedidos360.bff.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

// Reenvia a catalog. El token ya lo valido SecurityConfig, aqui solo se revisa el rol.
// OJO: el enunciado de la EV1 se contradice (seccion 15 vs tabla de la seccion 9).
// Segui la tabla: solo el Operador edita el catalogo. Si el profe dice que el Administrador
// tambien puede, agregar "ADMINISTRADOR" en los hasAnyRole de abajo.
@RestController
@RequestMapping("/api/catalog")
public class CatalogProxyController {

    private final RestClient catalogRestClient;

    public CatalogProxyController(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public List<Map<String, Object>> listar() {
        return catalogRestClient.get()
                .uri("/api/catalog")
                .retrieve()
                .body(List.class);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public Map<String, Object> obtener(@PathVariable Long id) {
        return catalogRestClient.get()
                .uri("/api/catalog/{id}", id)
                .retrieve()
                .body(Map.class);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR')")
    public Map<String, Object> crear(@RequestBody Map<String, Object> producto) {
        return catalogRestClient.post()
                .uri("/api/catalog")
                .body(producto)
                .retrieve()
                .body(Map.class);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public Map<String, Object> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> producto) {
        return catalogRestClient.put()
                .uri("/api/catalog/{id}", id)
                .body(producto)
                .retrieve()
                .body(Map.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OPERADOR')")
    public void eliminar(@PathVariable Long id) {
        catalogRestClient.delete()
                .uri("/api/catalog/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
