package co.uniquindio.tiendasana.dto.mesadtos;

import co.uniquindio.tiendasana.model.enums.EstadoMesa;


public record MesaInfoDTO(
        String id,
        String nombre,
        EstadoMesa estado,
        String localidad,
        float precioReserva,
        int capacidad,
        String imagen
) {
}
