package abaid.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearExchangeRequest(
        @NotBlank @Size(max = 200)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Solo letras, numeros, punto, guion y guion bajo")
        String nombre,
        @NotBlank @Pattern(regexp = "direct|topic|fanout", message = "Tipo debe ser direct, topic o fanout")
        String tipo,
        Boolean durable
) {}
