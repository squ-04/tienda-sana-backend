package co.uniquindio.tiendasana.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Mesas del catálogo para reservas (cliente).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tables")
public class TableDocument {

    public static final int DEFAULT_DURACION_RESERVA_MINUTOS = 120;

    @Id
    private String id;

    private String nombre;
    private String estado;
    private String localidad;
    private double precioReserva;
    private int capacidad;
    private String imagen;
    private Integer duracionReservaMinutos;

    @Builder.Default
    private boolean visibleToClient = true;
}
