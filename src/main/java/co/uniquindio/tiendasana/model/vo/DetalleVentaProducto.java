package co.uniquindio.tiendasana.model.vo;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DetalleVentaProducto {

    private String productoId;
    private int cantidad;
    private float valor;
    private String ventaId;

    @Builder
    public DetalleVentaProducto(String productoId, int cantidad, float valor,String ventaId) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.valor = valor;
        this.ventaId = ventaId;
    }
}
