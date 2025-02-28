package co.uniquindio.tiendasana.dto;

import co.uniquindio.tiendasana.model.enums.ProductStatus;
import co.uniquindio.tiendasana.model.enums.ProductType;

public record productDTO(
         String name,
         String description,
         String image,
         ProductType productType,
         ProductStatus status
) {
}
