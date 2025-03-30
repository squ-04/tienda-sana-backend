package co.uniquindio.tiendasana.model.vo;

import lombok.*;
import org.bson.types.ObjectId;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DetalleCarrito {
    private ObjectId productoId;
    private int cantidad;
    private float subtotal;

    @Builder
    public DetalleCarrito(ObjectId productoId, int cantidad, float subtotal) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

}