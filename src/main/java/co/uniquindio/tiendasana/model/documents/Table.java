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
    public Table(String name, TableStatus status,String locationId) {
        this.name = name;
        this.status = status;
        this.locationId = new ObjectId(locationId);
    }
}

/*Clase por hecha por el hijo de Robinson

package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.TableStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("tables")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Table {
    @Id
    @EqualsAndHashCode.Include
    private int id;
    @EqualsAndHashCode.Include
    private int number;
    private TableStatus status;

    @Builder
    public Table(int number, TableStatus status) {
        this.number = number;
        this.status = status;
    }
}
 */

