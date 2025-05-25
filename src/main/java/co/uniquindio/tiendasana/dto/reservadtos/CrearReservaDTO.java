package co.uniquindio.tiendasana.dto.reservadtos;

import co.uniquindio.tiendasana.dto.MesaDTO;

import java.time.LocalDateTime;
import java.util.List;

public record CrearReservaDTO(
        String emailUsuario,
        LocalDateTime fechaReserva,
        int cantidadPersonas,
        List<MesaDTO> mesas
) {
}
