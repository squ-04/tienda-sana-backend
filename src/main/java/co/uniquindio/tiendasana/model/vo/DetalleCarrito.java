package co.uniquindio.tiendasana.model.vo;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DetalleCarrito {
    private String productoId;
    private int cantidad;
    private float subtotal;

    @Builder
    public DetalleCarrito(String productoId, int cantidad, float subtotal) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

}