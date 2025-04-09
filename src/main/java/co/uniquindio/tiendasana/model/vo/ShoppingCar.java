package co.uniquindio.tiendasana.model.vo;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ShoppingCar {

    @EqualsAndHashCode.Include
    private String id;

    private LocalDateTime date;
    private String userId;
    private List<DetalleCarrito> products;

    @Builder
    public ShoppingCar(LocalDateTime date, String userId, List<DetalleCarrito> products) {
        this.date = date;
        this.userId = userId;
        this.products = products;
    }
}