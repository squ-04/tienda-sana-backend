package co.uniquindio.tiendasana.model.mongo;

import co.uniquindio.tiendasana.model.documents.Mesa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gestores_reservas")
public class GestorReservaDocument {

    @Id
    private String id;

    private LocalDateTime fecha;

    @Indexed
    private String emailUsuario;

    @Builder.Default
    private List<Mesa> mesas = new ArrayList<>();
}
