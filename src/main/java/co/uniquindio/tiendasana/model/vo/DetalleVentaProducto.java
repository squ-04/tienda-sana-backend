package co.uniquindio.tiendasana.model.vo;

import lombok.*;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DetalleVentaProducto that = (DetalleVentaProducto) o;
        return Objects.equals(productoId, that.productoId) && Objects.equals(ventaId, that.ventaId);
    }
}
