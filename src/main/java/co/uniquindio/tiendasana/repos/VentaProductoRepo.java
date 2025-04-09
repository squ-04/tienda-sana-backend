package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.VentaProducto;
import co.uniquindio.tiendasana.model.vo.DetalleVentaProducto;
import co.uniquindio.tiendasana.model.vo.Pago;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VentaProductoRepo {
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = "Ventas";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public VentaProductoRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<VentaProducto> obtenerVentas() throws IOException {
        List<List<Object>> filas = obtenerFilasHoja();
        return mapearFilasVentas(filas);
    }

    private List<List<Object>> obtenerFilasHoja() throws IOException {
        String rango = SHEET_NAME + "!A2:H"; // ID, UsuarioID, Productos, Fecha, Total, PromoID, CodigoPasarela, Pago
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return respuesta.getValues();
    }

    private List<VentaProducto> mapearFilasVentas(List<List<Object>> filas) {
        List<VentaProducto> ventas = new ArrayList<>();

        for (List<Object> row : filas) {
            try {
                String id = row.get(0).toString();
                String usuarioId = row.get(1).toString();

                List<DetalleVentaProducto> productos = objectMapper.readValue(
                        row.get(2).toString(),
                        new TypeReference<List<DetalleVentaProducto>>() {}
                );

                LocalDateTime fecha = LocalDateTime.parse(row.get(3).toString());
                float total = Float.parseFloat(row.get(4).toString());
                String promocionId = row.get(5).toString();
                String codigoPasarela = row.get(6).toString();

                Pago pago = objectMapper.readValue(
                        row.get(7).toString(),
                        Pago.class
                );

                VentaProducto venta = VentaProducto.builder()
                        .usuarioId(usuarioId)
                        .productos(productos)
                        .fecha(fecha)
                        .total(total)
                        .promocionId(promocionId)
                        .codigoPasarela(codigoPasarela)
                        .pago(pago)
                        .build();

                venta.setId(id); // Asignamos ID manualmente

                ventas.add(venta);

            } catch (Exception e) {
                System.err.println("❌ Error procesando fila de VentaProducto: " + row + "\n" + e.getMessage());
            }
        }

        return ventas;
    }
}
