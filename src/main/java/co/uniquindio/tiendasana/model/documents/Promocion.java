package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.TipoAplicacionPromocion;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Promocion {
    @EqualsAndHashCode.Include
    private String id;

    private String nombre;
    private String descripcion;
    private float porcentajeDescuento;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private TipoAplicacionPromocion tipoAplicacion;
    private List<String> idProductosConPromocion; //Se utiliza en caso de que el tipo de aplicacion sea por productos y aqui se guardarian los productoss especificaos con promocion

    @Builder
    public Promocion(String nombre, String descripcion, float porcentajeDescuento, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoAplicacionPromocion tipoAplicacion, List<String> idProductosConPromocion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoAplicacion = tipoAplicacion;
        this.idProductosConPromocion = idProductosConPromocion;
    }
}
