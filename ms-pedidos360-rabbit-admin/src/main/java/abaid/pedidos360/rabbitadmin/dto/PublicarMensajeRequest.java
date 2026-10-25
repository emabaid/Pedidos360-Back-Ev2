package abaid.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// El contenido es texto libre a proposito: asi se puede enviar un mensaje roto para probar la DLQ
public record PublicarMensajeRequest(
        @NotBlank String exchange,
        @NotNull String routingKey,
        @NotBlank String payload
) {}
