package abaid.pedidos360.catalog.service;

import abaid.pedidos360.catalog.messaging.StockEventoProducer;
import abaid.pedidos360.catalog.model.Product;
import abaid.pedidos360.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private ProductRepository repository;
    private StockEventoProducer producer;
    private ProductService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProductRepository.class);
        producer = mock(StockEventoProducer.class);
        service = new ProductService(repository, producer, 5);
        when(repository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Product producto(int stock) {
        Product p = new Product();
        p.setNombre("Mouse");
        p.setStock(stock);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    void disminuirStock_descuentaSinEventoSiQuedaSobreElUmbral() {
        producto(50);
        assertEquals(40, service.disminuirStock(1L, 10).getStock());
        verify(producer, never()).publicarStockBajo(any(), anyInt());
    }

    @Test
    void disminuirStock_publicaEventoSiQuedaEnElUmbral() {
        producto(8);
        service.disminuirStock(1L, 3);
        verify(producer).publicarStockBajo(any(Product.class), anyInt());
    }

    @Test
    void disminuirStock_rechazaStockInsuficiente() {
        producto(2);
        assertThrows(IllegalStateException.class, () -> service.disminuirStock(1L, 5));
        verify(producer, never()).publicarStockBajo(any(), anyInt());
    }
}
