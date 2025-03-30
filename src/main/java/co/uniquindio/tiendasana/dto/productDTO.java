package co.uniquindio.tiendasana.dto;

import co.uniquindio.tiendasana.model.enums.EstadoProducto;
import co.uniquindio.tiendasana.model.enums.CategoriaProducto;

public record productDTO(
         String name,
         String description,
         String image,
         CategoriaProducto categoriaProducto,
         EstadoProducto status
) {
}
