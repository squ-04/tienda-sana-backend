package co.uniquindio.tiendasana.model.mongo;

import co.uniquindio.tiendasana.model.vo.DetalleVentaProducto;
import co.uniquindio.tiendasana.model.vo.Pago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_sales")
public class VentaProductoDocument {

    @Id
    private String id;

    private String emailUsuario;
    private LocalDateTime fecha;
    private double total;
    private String promocionId;
    private String codigoPasarela;
    private Pago pago;

    @Builder.Default
    private List<DetalleVentaProducto> productos = new ArrayList<>();
}
