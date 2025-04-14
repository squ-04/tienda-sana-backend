package co.uniquindio.tiendasana.dto;

import co.uniquindio.tiendasana.model.enums.EstadoMesa;

public record mesaDTO(
        String name,
        EstadoMesa status,
        String locationName
) {
}
