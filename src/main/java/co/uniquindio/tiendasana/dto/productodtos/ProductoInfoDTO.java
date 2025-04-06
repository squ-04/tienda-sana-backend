package co.uniquindio.tiendasana.dto.productodtos;

/**
 * DTO el cual va a ser usado para tranferir informacion como detalle de producto
 * al cliente
 * @param id
 * @param nombre
 * @param categoria
 * @param imagen
 * @param precioUnitario
 * @param cantidad
 */
public record ProductoInfoDTO (
        String id,
        String nombre,
        String categoria,
        String imagen,
        float precioUnitario,
        int cantidad
){

}
