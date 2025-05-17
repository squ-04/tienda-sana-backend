package co.uniquindio.tiendasana.dto.mesadtos;

import java.util.List;

public record ListaMesasDTO(
        int totalPaginas,
        List<MesaItemDTO> mesas
) {
}
