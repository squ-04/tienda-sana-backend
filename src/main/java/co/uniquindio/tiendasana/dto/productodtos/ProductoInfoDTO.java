package co.uniquindio.tiendasana.dto.productodtos;

public record ProductoInfoDTO (
        String id,
        String nombre,
        String categoria,
        String imagen,
        float precioUnitario,
        int cantidad
){

}
