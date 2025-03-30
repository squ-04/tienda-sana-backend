package co.uniquindio.tiendasana.model.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("locations")
@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Location {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String name;
    private String description;

    @Builder
    public Location(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
