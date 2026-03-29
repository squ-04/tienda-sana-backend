package co.uniquindio.tiendasana.dto;

import co.uniquindio.tiendasana.model.enums.EstadoMesa;

public record MesaDTO(
        String id,
        String nombre,
        String estado,
        String localidad,
        float precioReserva,
        int capacidad,
        String imagen,
        Integer duracionReservaMinutos,
        String idReserva,
        String idGestorReserva
) {
}
