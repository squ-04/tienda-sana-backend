package co.uniquindio.tiendasana.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "suppliers")
public class SupplierDocument {

    @Id
    private String id;

    private String category;
    private String name;

    /** Producto o insumo que suministra (campo "product" en JSON). */
    private String product;

    private String contact;
    private String address;
    private String city;

    @Builder.Default
    @Indexed
    private boolean active = true;
}
