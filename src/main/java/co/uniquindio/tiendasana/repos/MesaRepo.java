package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.MesaDTO;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.utils.MesaConstantes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import org.springframework.stereotype.Repository;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class MesaRepo {
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = MesaConstantes.HOJA;

    public MesaRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<MesaDTO> obtenerMesas() throws IOException {
        List<List<Object>> filas = obtenerFilasHoja();
        return mapearFilasMesas(filas);
    }

    private List<List<Object>> obtenerFilasHoja() throws IOException {
        String rango = SHEET_NAME + "!A2:"+ MesaConstantes.COL_REGISTRO_FINAL; // ID, Nombre, Estado, Localidad, PrecioReserva
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
    }

    private List<MesaDTO> mapearFilasMesas(List<List<Object>> filas) {
        List<MesaDTO> mesas = new ArrayList<>();

        for (List<Object> row : filas) {
            try {
                MesaDTO mesa=mapearMesa(row);
                mesas.add(mesa);
            } catch (Exception e) {
                System.err.println("❌ Error al procesar fila de Mesa: " + row + "\n" + e.getMessage());
            }
        }

        return mesas;
    }

    public MesaDTO mapearMesa(List<Object> row) {
        String nombre = row.get(0).toString();
        EstadoMesa estado = EstadoMesa.valueOf(row.get(1).toString().toUpperCase().replace(" ", "_"));
        int capacidad = Integer.parseInt(row.get(2).toString());
        String localidad = row.get(3).toString();
        float precioReserva = Float.parseFloat(row.get(4).toString());
        String imagen = row.get(5).toString();

        MesaDTO mesa = MesaDTO.builder()
                .nombre(nombre)
                .estado(estado)
                .capacidad(capacidad)
                .localidad(localidad)
                .precioReserva(precioReserva)
                .imagen(imagen)
                .build();
        mesa.setId(row.get(6).toString());

        return mesa;
    }

    //TODO verificar si es valido para el estado y precio
    public List<Object> mapearMesaInverso(MesaDTO mesa) {
        return Arrays.asList(
                mesa.getNombre(),
                mesa.getEstado(),
                mesa.getCapacidad(),
                mesa.getLocalidad(),
                ""+((int)mesa.getPrecioReserva()),
                mesa.getImagen()
        );
    }

    public List<MesaDTO> filtrar (Predicate<MesaDTO> expresion) throws IOException {
        List<MesaDTO> mesas = obtenerMesas();
        return mesas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    public int obtenerIndiceMesa(String id) {
        List<MesaDTO> mesas = null;
        int filaCuenta=-1;
        try {
            mesas = obtenerMesas();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        int tam=mesas.size();
        for (int i=0;i<tam;i++) {
            if (mesas.get(i).getId().equals(id)) {
                filaCuenta=i;
                break;
            }
        }
        return filaCuenta;
    }

    public void actualizar(MesaDTO mesa) throws IOException {
        int indice=obtenerIndiceMesa(mesa.getId());
        if (indice!=-1) {
            String range = SHEET_NAME+"!A"+(2+indice)+":"+ MesaConstantes.COL_REGISTRO_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearMesaInverso(mesa)
            );

            ValueRange body = new ValueRange().setValues(values);

            UpdateValuesResponse result = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW") // "RAW" para insertar como está
                    .execute();

            System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
        } else {
            throw new IOException("Registro no encontrado");
        }
    }

}
