package co.uniquindio.tiendasana.model.vo;

import co.uniquindio.tiendasana.model.documents.Mesa;
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

    private LocalDateTime date;
    private String emailUsuario;
    private List<Mesa> mesas;

    @Builder
    public GestorReservas(LocalDateTime date, String emailUsuario, List<Mesa> mesas) {
        this.date = date;
        this.emailUsuario = emailUsuario;
        this.mesas = mesas;
    }
}
