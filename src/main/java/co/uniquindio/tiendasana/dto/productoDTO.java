package co.uniquindio.tiendasana.dto;

import co.uniquindio.tiendasana.model.enums.EstadoProducto;
import co.uniquindio.tiendasana.model.enums.CategoriaProducto;

public record productoDTO(
         String nombre,
         String descripcion,
         float precio,
         int cantidad,
         String imagenUrl,
         CategoriaProducto categoriaProducto,
         float calificacionPromedio,
         EstadoProducto status
) {
}
