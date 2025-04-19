package co.uniquindio.tiendasana.dto.carritoCompras;

public record VistaItemCarritoDTO(
        String idProducto,
        String nombreProducto,
        String categoria,
        float precio,
        int cantidad,
        float total
) {
}
