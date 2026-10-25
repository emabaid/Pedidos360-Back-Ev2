package abaid.pedidos360.rabbitadmin.controller;

import abaid.pedidos360.rabbitadmin.dto.*;
import abaid.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Servicio interno: el BFF solo deja pasar a ADMINISTRADOR.
// Este controller solo recibe y valida; la logica esta en RabbitAdminService.
@RestController
@RequestMapping("/api/admin/rabbit")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    // colas
    @GetMapping("/queues")
    public List<ColaInfo> listarColas() {
        return service.listarColas();
    }

    @GetMapping("/queues/{nombre}")
    public ColaInfo infoCola(@PathVariable String nombre) {
        return service.infoCola(nombre);
    }

    @PostMapping("/queues")
    @ResponseStatus(HttpStatus.CREATED)
    public ColaInfo crearCola(@Valid @RequestBody CrearColaRequest request) {
        return service.crearCola(request);
    }

    @DeleteMapping("/queues/{nombre}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCola(@PathVariable String nombre) {
        service.eliminarCola(nombre);
    }

    @DeleteMapping("/queues/{nombre}/messages")
    public Map<String, Object> purgarCola(@PathVariable String nombre) {
        return Map.of("cola", nombre, "mensajesEliminados", service.purgarCola(nombre));
    }

    // exchanges
    @GetMapping("/exchanges")
    public List<ExchangeInfo> listarExchanges() {
        return service.listarExchanges();
    }

    @PostMapping("/exchanges")
    @ResponseStatus(HttpStatus.CREATED)
    public void crearExchange(@Valid @RequestBody CrearExchangeRequest request) {
        service.crearExchange(request);
    }

    @DeleteMapping("/exchanges/{nombre}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarExchange(@PathVariable String nombre) {
        service.eliminarExchange(nombre);
    }

    // bindings
    @GetMapping("/bindings")
    public List<BindingInfo> listarBindings() {
        return service.listarBindings();
    }

    @PostMapping("/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    public void crearBinding(@Valid @RequestBody BindingRequest request) {
        service.crearBinding(request);
    }

    @DeleteMapping("/bindings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarBinding(@Valid @RequestBody BindingRequest request) {
        service.eliminarBinding(request);
    }

    // mensajes de prueba
    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void publicar(@Valid @RequestBody PublicarMensajeRequest request) {
        service.publicar(request);
    }
}
