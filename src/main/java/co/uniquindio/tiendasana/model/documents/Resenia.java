package co.uniquindio.tiendasana.model.documents;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Resenia {
    @EqualsAndHashCode.Include
    private String id;

    private String usuarioId;
    private String productoId;
    private String comentario;
    private int calificacion; //Se manejara con una escala de 1 a 5
    private LocalDateTime fecha;

    @Builder
    public Resenia(String usuarioId, String productoId, String comentario, int calificacion, LocalDateTime fecha) {
        this.usuarioId = usuarioId;
        this.productoId = productoId;
        this.comentario = comentario;
        this.calificacion = calificacion;
        this.fecha = fecha;
    }
}
