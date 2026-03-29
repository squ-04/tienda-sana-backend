package co.uniquindio.tiendasana.dto.admin;

/**
 * Respuesta alineada con el catálogo de mesas del cliente (mismos campos que {@code TableDocument}).
 */
public record RestaurantTableResponse(
        String id,
        String nombre,
        String estado,
        String localidad,
        double precioReserva,
        int capacidad,
        String imagen,
        boolean visibleToClient
) {
}
