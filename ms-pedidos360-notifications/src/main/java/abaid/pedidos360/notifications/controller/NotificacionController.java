package abaid.pedidos360.notifications.controller;

import abaid.pedidos360.notifications.model.DeadLetterRegistro;
import abaid.pedidos360.notifications.model.Notificacion;
import abaid.pedidos360.notifications.service.DeadLetterService;
import abaid.pedidos360.notifications.service.NotificacionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Servicio interno. El BFF decide, segun el token, que notificaciones puede ver cada usuario.
@RestController
@RequestMapping("/api/notifications")
public class NotificacionController {

    private final NotificacionService notificaciones;
    private final DeadLetterService deadLetters;

    public NotificacionController(NotificacionService notificaciones, DeadLetterService deadLetters) {
        this.notificaciones = notificaciones;
        this.deadLetters = deadLetters;
    }

    @GetMapping
    public List<Notificacion> todas() {
        return notificaciones.listarTodas();
    }

    @GetMapping("/mias")
    public List<Notificacion> mias(@RequestParam String usuario) {
        return notificaciones.listarPorUsuario(usuario);
    }

    @GetMapping("/dead-letters")
    public List<DeadLetterRegistro> deadLetters() {
        return deadLetters.listar();
    }
}
