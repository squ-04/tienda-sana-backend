package co.uniquindio.tiendasana.model.documents;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class GestorReservas {

    @EqualsAndHashCode.Include
    private String id;

    private LocalDateTime fecha;
    private String emailUsuario;
    private List<Mesa> mesas;

    @Builder
    public GestorReservas(String id, LocalDateTime fecha, String emailUsuario, List<Mesa> mesas) {
        this.id = id;
        this.fecha = fecha;
        this.emailUsuario = emailUsuario;
        this.mesas = mesas;
    }
}
