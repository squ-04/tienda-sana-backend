package co.uniquindio.tiendasana.dto.carritoComprasdtos;

public record VistaItemCarritoDTO(
        String idProducto,
        String nombreProducto,
        String categoria,
        float precio,
        int cantidad,
        float total
) {
}
