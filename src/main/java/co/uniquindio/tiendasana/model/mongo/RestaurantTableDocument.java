package co.uniquindio.tiendasana.model.mongo;

import co.uniquindio.tiendasana.model.enums.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Mesas gestionadas desde el panel admin.
 * Las mesas de reservas del cliente están en la colección {@code reservation_tables}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "restaurant_tables")
public class RestaurantTableDocument {

    @Id
    private String id;

    private int capacity;
    private String location;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;
}
