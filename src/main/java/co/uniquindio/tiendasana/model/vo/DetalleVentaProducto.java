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

    @Builder
    public DetalleVentaProducto(String productoId, int cantidad, float valor) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.valor = valor;
    }
}
