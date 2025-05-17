package co.uniquindio.tiendasana.dto.productodtos;

import co.uniquindio.tiendasana.model.documents.Producto;

import java.util.List;

public record ProductosTotalDTO(
        int totalProductos,
        List<Producto> productos
) {
}
