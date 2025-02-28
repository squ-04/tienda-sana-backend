package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.ProductStatus;
import co.uniquindio.tiendasana.model.enums.ProductType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("products")
@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String name;
    private String description;
    private String image;
    private ProductType productType;
    private ProductStatus status;

    @Builder
    public Product(String name, String description, String image, ProductType productType, ProductStatus status) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.productType = productType;
        this.status = status;
    }
}