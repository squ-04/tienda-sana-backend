package co.uniquindio.tiendasana.dto.mesadtos;

import co.uniquindio.tiendasana.model.enums.EstadoMesa;

public record MesaItemDTO(
        String id,
        String nombre,
        String estado,
        String localidad,
        float precioReserva,
        int capacidad,
        String imagen,
        int duracionReservaMinutos
) {
}
