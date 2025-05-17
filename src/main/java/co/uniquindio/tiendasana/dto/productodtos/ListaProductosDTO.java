package co.uniquindio.tiendasana.dto.productodtos;

import java.util.List;

public record ListaProductosDTO(
        int totalPaginas,
        List<ProductoItemDTO> productos
) {
}
