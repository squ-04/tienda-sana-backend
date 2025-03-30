package co.uniquindio.tiendasana.model.vo;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ShoppingCart {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    private LocalDateTime date;
    private ObjectId userId;
    private List<DetalleCarrito> products;

    @Builder
    public ShoppingCart(LocalDateTime date, ObjectId userId, List<DetalleCarrito> products) {
        this.date = date;
        this.userId = userId;
        this.products = products;
    }
}