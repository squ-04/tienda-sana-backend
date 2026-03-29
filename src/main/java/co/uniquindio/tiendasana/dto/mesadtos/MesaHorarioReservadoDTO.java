package co.uniquindio.tiendasana.dto.mesadtos;

import java.time.LocalDateTime;

public record MesaHorarioReservadoDTO(
        LocalDateTime inicio,
        LocalDateTime fin
) {
}
