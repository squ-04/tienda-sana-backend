package co.uniquindio.tiendasana.dto.productodtos;

import java.util.List;

public record ListaProductos(
        int totalPaginas,
        List<ProductoItemDTO> productos
) {
}
