package abaid.pedidos360.notifications.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;
    private String tipo;
    private String destinatario;

    @Column(length = 500)
    private String mensaje;

    private LocalDateTime fecha = LocalDateTime.now();

    public Notificacion() {}

    public Notificacion(Long pedidoId, String tipo, String destinatario, String mensaje) {
        this.pedidoId = pedidoId;
        this.tipo = tipo;
        this.destinatario = destinatario;
        this.mensaje = mensaje;
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public String getTipo() { return tipo; }
    public String getDestinatario() { return destinatario; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFecha() { return fecha; }
}
