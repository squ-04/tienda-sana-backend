package co.uniquindio.tiendasana.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alta/edición de mesa en el mismo modelo que ve el cliente ({@code tables}).
 */
public record RestaurantTableRequest(
        @NotBlank @Size(max = 200) String nombre,
        @NotBlank String estado,
        @NotBlank @Size(max = 120) String localidad,
        @NotNull @Min(0) Double precioReserva,
        @NotNull @Min(1) Integer capacidad,
        @NotBlank @Size(max = 2000) String imagen,
        @NotNull Boolean visibleToClient
) {
}
