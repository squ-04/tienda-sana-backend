package co.uniquindio.tiendasana.dto.reservadtos;

import java.time.LocalDateTime;
import java.util.Date;

public record ActualizarReservaDTO(
        String reservaId,
        LocalDateTime fechaReserva,
        int cantidadPersonas
) {
}
