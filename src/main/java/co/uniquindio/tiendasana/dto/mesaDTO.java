package co.uniquindio.tiendasana.dto;

import co.uniquindio.tiendasana.model.enums.EstadoMesa;

public record mesaDTO(
        String id,
        String nombre,
        EstadoMesa estado,
        String localidad,
        float precioReserva,
        int capacidad,
        String imagen

) {
}
