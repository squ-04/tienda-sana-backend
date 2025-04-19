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
    public CarritoCompras(String id,LocalDateTime fecha, String idUsuario, List<DetalleCarrito> productos) {
        this.id = id;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.productos = productos;
    }

    public void agregarDetalle(DetalleCarrito detalleCarrito) {
        if (detalleCarrito != null) {
            boolean detalleEncontrado=false;
            for (DetalleCarrito detalle:this.productos) {
                if (detalle.equals(detalleCarrito)) {
                    detalleEncontrado=true;
                    float precioUnitario=detalle.getSubtotal()/detalle.getCantidad();
                    detalle.setCantidad(detalle.getCantidad()+detalleCarrito.getCantidad());
                    detalle.setSubtotal(detalle.getCantidad()*precioUnitario);
                }
            }
            if (!detalleEncontrado) {
                this.productos.add(detalleCarrito);
            }
        } else {
            this.productos=List.of(detalleCarrito);
        }
    }

}
