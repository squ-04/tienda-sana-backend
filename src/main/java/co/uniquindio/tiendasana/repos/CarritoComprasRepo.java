package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CarritoComprasRepo {

    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = "CarritoCompras"; // nombre de tu hoja en Google Sheets

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CarritoComprasRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<CarritoCompras> obtenerCarritos() throws IOException {
        List<List<Object>> filas = obtenerFilasHoja();
        return mapearFilasCarritos(filas);
    }

    private List<List<Object>> obtenerFilasHoja() throws IOException {
        String rango = SHEET_NAME + "!A2:C"; // Asumiendo que usas columnas: Fecha, IDUsuario, Productos
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return respuesta.getValues();
    }

    private List<CarritoCompras> mapearFilasCarritos(List<List<Object>> filas) {
        List<CarritoCompras> carritos = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                LocalDateTime fecha = LocalDateTime.parse(row.get(0).toString());
                String idUsuario = row.get(1).toString();
                String productosJson = row.get(2).toString();

                List<DetalleCarrito> productos = objectMapper.readValue(
                        productosJson, new TypeReference<List<DetalleCarrito>>() {});

                CarritoCompras carrito = CarritoCompras.builder()
                        .fecha(fecha)
                        .idUsuario(idUsuario)
                        .productos(productos)
                        .build();

                carritos.add(carrito);
            } catch (Exception e) {
                System.err.println("Error al procesar fila: " + row + "\n" + e.getMessage());
            }
        }
        return carritos;
    }
}
