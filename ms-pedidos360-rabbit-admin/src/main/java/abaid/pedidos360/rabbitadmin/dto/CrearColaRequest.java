package abaid.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearColaRequest(
        @NotBlank @Size(max = 200)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Solo letras, numeros, punto, guion y guion bajo")
        String nombre,
        Boolean durable,
        // true: ademas crea una cola de errores llamada "<nombre>.dlq" y la conecta a esta cola
        Boolean conDlq,
        @Min(1000) Long ttlMs,
        @Min(1) Integer maxLength
) {}
