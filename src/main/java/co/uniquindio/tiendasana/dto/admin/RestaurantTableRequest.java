package co.uniquindio.tiendasana.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestaurantTableRequest(
        @NotNull @Min(1) Integer capacity,
        @NotBlank String location,
        @NotNull Boolean active
) {
}
