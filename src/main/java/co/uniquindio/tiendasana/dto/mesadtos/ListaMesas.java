package co.uniquindio.tiendasana.dto.mesadtos;

import java.util.List;

public record ListaMesas(
        int totalPaginas,
        List<MesaItemDTO> mesas
) {
}
