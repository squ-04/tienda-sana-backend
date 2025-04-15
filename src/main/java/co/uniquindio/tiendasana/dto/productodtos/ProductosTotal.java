package co.uniquindio.tiendasana.dto.productodtos;

import co.uniquindio.tiendasana.model.documents.Producto;

import java.util.List;

public record ProductosTotal(
        int totalProductos,
        List<Producto> productos
) {
}
