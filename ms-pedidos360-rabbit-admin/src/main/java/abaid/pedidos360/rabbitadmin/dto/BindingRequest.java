package abaid.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;

public record BindingRequest(
        @NotBlank String cola,
        @NotBlank String exchange,
        // en un exchange fanout no se usa, puede ir vacio
        String routingKey
) {}
