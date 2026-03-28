package co.uniquindio.tiendasana.model.mongo;

import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.vo.Pago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservations")
public class ReservaDocument {

    @Id
    private String id;

    private String usuarioId;

    @Builder.Default
    private List<Mesa> mesas = new ArrayList<>();

    private LocalDateTime fechaReserva;
    private double valorReserva;
    private int cantidadPersonas;
    private String estadoReserva;
    private String codigoPasarela;
    private Pago pago;
}
