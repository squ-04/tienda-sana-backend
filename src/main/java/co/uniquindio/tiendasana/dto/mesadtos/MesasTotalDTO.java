package co.uniquindio.tiendasana.dto.mesadtos;

import co.uniquindio.tiendasana.model.documents.MesaDTO;

import java.util.List;

public record MesasTotalDTO (
        int totalMesas,
        List<MesaDTO> mesas
){

}
