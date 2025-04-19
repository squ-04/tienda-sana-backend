package co.uniquindio.tiendasana.model.vo;

import lombok.*;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DetalleCarrito {
    private String productoId;
    private int cantidad;
    private float subtotal;
    private String idCarrito;

    @Builder
    public DetalleCarrito(String productoId, int cantidad, float subtotal,String idCarrito) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.idCarrito = idCarrito;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DetalleCarrito that = (DetalleCarrito) o;
        return Objects.equals(productoId, that.productoId) && Objects.equals(idCarrito, that.idCarrito);
    }
}