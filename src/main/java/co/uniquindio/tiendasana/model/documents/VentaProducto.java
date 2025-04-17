package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.vo.DetalleVentaProducto;
import co.uniquindio.tiendasana.model.vo.Pago;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VentaProducto {
    @EqualsAndHashCode.Include
    private String id;

    private String emailUsario;
    private List<DetalleVentaProducto> productos;
    private LocalDateTime fecha;
    private float total;
    private String promocionId;
    private String codigoPasarela;
    private Pago pago;

    @Builder
    public VentaProducto(String id,String emailUsario, List<DetalleVentaProducto> productos, LocalDateTime fecha, float total, String promocionId, String codigoPasarela, Pago pago) {
        this.id = id;
        this.emailUsario = emailUsario;
        this.productos = productos;
        this.fecha = fecha;
        this.total = total;
        this.promocionId = promocionId;
        this.codigoPasarela = codigoPasarela;
        this.pago = pago;
    }
}
