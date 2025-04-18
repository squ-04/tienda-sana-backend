package co.uniquindio.tiendasana.dto.carritoCompras;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record EditarDetalleCarritoDTO(
        @NotBlank(message = "User ID cannot be empty")
        String idUsuario,

        @NotBlank(message = "Event ID cannot be empty")
        String idProducto,

        @Min(value = 1, message = "Amount must be at least 1")
        int cantidad
) {
}
