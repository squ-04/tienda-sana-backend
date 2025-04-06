package co.uniquindio.tiendasana.dto.productodtos;

/**
 * DTO usado para mostrar caracteristicas iniciales de los productos
 * al cliente
 * @param id
 * @param nombre
 * @param categoria
 * @param imagen
 * @param precioUnitario
 */
public record ProductoItemDTO (
        String id,
        String nombre,
        String categoria,
        String imagen,
        float precioUnitario
){
}
