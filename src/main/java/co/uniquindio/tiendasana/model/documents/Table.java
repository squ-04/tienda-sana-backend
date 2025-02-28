package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.ProductStatus;
import co.uniquindio.tiendasana.model.enums.ProductType;
import co.uniquindio.tiendasana.model.enums.TableStatus;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("tables")
@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Table {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String name;
    private TableStatus status;
    private ObjectId locationId;

    @Builder
    public Table(String name, TableStatus status,ObjectId locationId) {
        this.name = name;
        this.status = status;
        this.locationId = locationId;
    }
}

