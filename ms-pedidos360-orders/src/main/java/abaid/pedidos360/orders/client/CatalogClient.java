package abaid.pedidos360.orders.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Llama a catalog desde orders (red interna). El navegador nunca llega aqui directo.
@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(@Value("${pedidos360.catalog-base-url}") String catalogBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(catalogBaseUrl).build();
    }

    public ProductoInfo obtenerProducto(Long productoId) {
        return restClient.get()
                .uri("/api/catalog/{id}", productoId)
                .retrieve()
                .body(ProductoInfo.class);
    }

    public void disminuirStock(Long productoId, int cantidad) {
        restClient.patch()
                .uri("/api/catalog/{id}/stock/disminuir", productoId)
                .body(new StockUpdateBody(cantidad))
                .retrieve()
                .toBodilessEntity();
    }

    public void aumentarStock(Long productoId, int cantidad) {
        restClient.patch()
                .uri("/api/catalog/{id}/stock/aumentar", productoId)
                .body(new StockUpdateBody(cantidad))
                .retrieve()
                .toBodilessEntity();
    }
}
