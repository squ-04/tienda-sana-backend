package co.uniquindio.tiendasana.dto.carritoComprasdtos;

import jakarta.validation.constraints.NotBlank;

public record BorrarDetalleCarritoDTO (
        @NotBlank(message = "User ID cannot be empty")
        String idUsuario,

        @NotBlank(message = "Event ID cannot be empty")
        String idProducto
) {
}
