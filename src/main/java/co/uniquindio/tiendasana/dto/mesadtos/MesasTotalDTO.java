package co.uniquindio.tiendasana.dto.mesadtos;

import co.uniquindio.tiendasana.model.documents.Mesa;

import java.util.List;

public record MesasTotalDTO (
        int totalMesas,
        List<Mesa> mesas
){

}
