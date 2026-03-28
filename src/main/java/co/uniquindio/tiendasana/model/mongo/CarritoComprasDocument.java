package co.uniquindio.tiendasana.model.mongo;

import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shopping_carts")
public class CarritoComprasDocument {

    @Id
    private String id;

    private LocalDateTime fecha;

    @Indexed
    private String idUsuario;

    @Builder.Default
    private List<DetalleCarrito> productos = new ArrayList<>();
}
