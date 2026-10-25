package abaid.pedidos360.catalog.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import abaid.pedidos360.catalog.messaging.StockEventoProducer;
import abaid.pedidos360.catalog.model.Product;
import abaid.pedidos360.catalog.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final StockEventoProducer eventoProducer;
    private final int umbralBajo;

    public ProductService(ProductRepository repository, StockEventoProducer eventoProducer,
            @Value("${pedidos360.stock.umbral-bajo:5}") int umbralBajo) {
        this.repository = repository;
        this.eventoProducer = eventoProducer;
        this.umbralBajo = umbralBajo;
    }

    public List<Product> listar() {
        return repository.findAll();
    }

    public Product obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + id));
    }

    public Product crear(Product producto) {
        return repository.save(producto);
    }

    public Product actualizar(Long id, Product datos) {
        Product existente = obtener(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        return repository.save(existente);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    public Product disminuirStock(Long id, int cantidad) {
        Product producto = obtener(id);
        if (producto.getStock() < cantidad) {
            throw new IllegalStateException(
                    "Stock insuficiente para el producto " + id
                            + " (disponible: " + producto.getStock() + ", solicitado: " + cantidad + ")");
        }
        producto.setStock(producto.getStock() - cantidad);
        Product guardado = repository.save(producto);
        if (guardado.getStock() <= umbralBajo) {
            eventoProducer.publicarStockBajo(guardado, umbralBajo);
        }
        return guardado;
    }

    public Product aumentarStock(Long id, int cantidad) {
        Product producto = obtener(id);
        producto.setStock(producto.getStock() + cantidad);
        return repository.save(producto);
    }
}
