package co.uniquindio.tiendasana.model.vo;

import lombok.*;
import org.bson.types.ObjectId;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class ProductDetail {
    private ObjectId productId;
    private int quantity;
    private float subtotal;

    @Builder
    public ProductDetail(ObjectId productId, int quantity, float subtotal) {
        this.productId = productId;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

}