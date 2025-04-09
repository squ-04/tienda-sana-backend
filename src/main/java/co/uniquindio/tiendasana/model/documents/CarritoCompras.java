package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CarritoCompras {

    @EqualsAndHashCode.Include
    private String id;

    private LocalDateTime fecha;
    private String idUsuario;
    private List<DetalleCarrito> productos;

    @Builder
    public CarritoCompras(LocalDateTime fecha, String idUsuario, List<DetalleCarrito> productos) {
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.productos = productos;
    }

}
