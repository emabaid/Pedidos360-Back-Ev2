package abaid.pedidos360.notifications.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dead_letters")
public class DeadLetterRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String colaOrigen;
    private String motivo;
    private Integer intentos;

    @Column(length = 4000)
    private String payload;

    private LocalDateTime fecha = LocalDateTime.now();

    public DeadLetterRegistro() {}

    public DeadLetterRegistro(String colaOrigen, String motivo, Integer intentos, String payload) {
        this.colaOrigen = colaOrigen;
        this.motivo = motivo;
        this.intentos = intentos;
        this.payload = payload;
    }

    public Long getId() { return id; }
    public String getColaOrigen() { return colaOrigen; }
    public String getMotivo() { return motivo; }
    public Integer getIntentos() { return intentos; }
    public String getPayload() { return payload; }
    public LocalDateTime getFecha() { return fecha; }
}
