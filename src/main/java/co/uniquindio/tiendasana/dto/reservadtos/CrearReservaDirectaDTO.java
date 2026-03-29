package co.uniquindio.tiendasana.dto.reservadtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CrearReservaDirectaDTO(
        @NotBlank String emailUsuario,
        @NotBlank String mesaId,
        @NotNull @Future LocalDateTime fechaReserva,
        @NotNull @Min(1) Integer cantidadPersonas
) {
}
