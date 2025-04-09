package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import org.springframework.stereotype.Repository;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MesaRepo {
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = "Mesas";

    public MesaRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<Mesa> obtenerMesas() throws IOException {
        List<List<Object>> filas = obtenerFilasHoja();
        return mapearFilasMesas(filas);
    }

    private List<List<Object>> obtenerFilasHoja() throws IOException {
        String rango = SHEET_NAME + "!A2:E"; // ID, Nombre, Estado, Localidad, PrecioReserva
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return respuesta.getValues();
    }

    private List<Mesa> mapearFilasMesas(List<List<Object>> filas) {
        List<Mesa> mesas = new ArrayList<>();

        for (List<Object> row : filas) {
            try {
                String id = row.get(0).toString();
                String nombre = row.get(1).toString();
                EstadoMesa estado = EstadoMesa.valueOf(row.get(2).toString().toUpperCase().replace(" ", "_"));
                String localidad = row.get(3).toString();
                float precioReserva = Float.parseFloat(row.get(4).toString());

                Mesa mesa = Mesa.builder()
                        .nombre(nombre)
                        .estado(estado)
                        .localidad(localidad)
                        .precioReserva(precioReserva)
                        .build();

                mesa.setId(id); // porque el builder no incluye el id

                mesas.add(mesa);
            } catch (Exception e) {
                System.err.println("❌ Error al procesar fila de Mesa: " + row + "\n" + e.getMessage());
            }
        }

        return mesas;
    }

}
