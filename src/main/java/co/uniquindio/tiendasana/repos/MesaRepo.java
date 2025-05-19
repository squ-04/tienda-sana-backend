package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.dto.mesadtos.MesasTotalDTO;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.utils.MesaConstantes;
import co.uniquindio.tiendasana.utils.ProductoConstantes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import org.springframework.stereotype.Repository;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    public List<Mesa> obtenerMesas() throws IOException {
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

    public MesasTotalDTO obtenerMesas(int pagina, int cantidadElementos) throws IOException {
        int totalMesas= contarMesasExistentes();
        List<List<Object>> filas = obtenerFilasHoja(pagina,cantidadElementos,totalMesas);
        List<Mesa> mesas = mapearFilasMesas(filas);
        return new MesasTotalDTO( totalMesas, mesas);
    }

    public int contarMesasExistentes() throws IOException {
        String rango = SHEET_NAME + MesaConstantes.CANT_MESAS;
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());

    }

    private List<List<Object>> obtenerFilasHoja(int pagina, int cantidad, int cantidadTotal) throws IOException {
        int cantidadPaginas = (int) Math.ceil((double) cantidadTotal / cantidad);

        if (pagina < 0 || pagina >= cantidadPaginas) {
            throw new IllegalArgumentException("La página no existe");
        }
        int filaInicio = 2 + (pagina * cantidad);
        int filaFin = filaInicio + cantidad - 1;

        String rango = SHEET_NAME + "!A" + filaInicio + ":G" + filaFin;

        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();

        if (respuesta.getValues() == null || respuesta.getValues().isEmpty()) {
            return new ArrayList<>();
        }

        return respuesta.getValues();
    }


    private List<Mesa> mapearFilasMesas(List<List<Object>> filas) {
        List<Mesa> mesas = new ArrayList<>();

        for (List<Object> row : filas) {
            try {
                Mesa mesa=mapearMesa(row);
                mesas.add(mesa);
            } catch (Exception e) {
                System.err.println("❌ Error al procesar fila de Mesa: " + row + "\n" + e.getMessage());
            }
        }

        return mesas;
    }

    public Mesa mapearMesa(List<Object> row) {
        String nombre = row.get(0).toString();
        EstadoMesa estado = EstadoMesa.valueOf(row.get(1).toString().toUpperCase().replace(" ", "_"));
        int capacidad = Integer.parseInt(row.get(2).toString());
        String localidad = row.get(3).toString();
        float precioReserva = Float.parseFloat(row.get(4).toString());
        String imagen = row.get(5).toString();
        String idReserva = row.get(6).toString();
        String idGestorReserva = row.get(7).toString();

        Mesa mesa = Mesa.builder()
                .nombre(nombre)
                .estado(estado)
                .capacidad(capacidad)
                .localidad(localidad)
                .precioReserva(precioReserva)
                .imagen(imagen)
                .idReserva(idReserva)
                .idGestorReserva(idGestorReserva)
                .build();
        mesa.setId(row.get(6).toString());

        return mesa;
    }

    //TODO verificar si es valido para el estado y precio
    public List<Object> mapearMesaInverso(Mesa mesa) {
        return Arrays.asList(
                mesa.getNombre(),
                mesa.getEstado(),
                mesa.getCapacidad(),
                mesa.getLocalidad(),
                ""+((int)mesa.getPrecioReserva()),
                mesa.getImagen(),
                mesa.getIdReserva(),
                mesa.getIdGestorReserva()
        );
    }

    public List<Mesa> filtrar (Predicate<Mesa> expresion) throws IOException {
        List<Mesa> mesas = obtenerMesas();
        return mesas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    public List<Mesa> filtrar (Predicate<Mesa> expresion, String hoja) throws IOException {
        List<Mesa> mesas = obtenerMesas(hoja);
        return mesas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    private List<List<Object>> obtenerFilasHoja(String hoja) throws IOException {
        String rango = hoja + "!A2:"+ MesaConstantes.COL_REGISTRO_FINAL;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
    }

    public List<Mesa> obtenerMesas(String hoja) throws IOException {
        List<List<Object>> filas = obtenerFilasHoja(hoja);
        return mapearFilasMesas(filas);
    }

    public int obtenerIndiceMesa(String id) {
        List<Mesa> mesas = null;
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

    public void actualizar(Mesa mesa) throws IOException {
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

    public Optional<Mesa> obtenerPorId(String id) throws IOException {
        List<Mesa> mesasObtenidas=
                filtrar(mesa -> mesa.getId().equals(id), SHEET_NAME);
        if (mesasObtenidas.isEmpty()) {
            return Optional.empty();
        }
        if (mesasObtenidas.size()>1) {
            throw new IOException("Mas de una mesa tiene ese id");
        }
        return Optional.of(mesasObtenidas.get(0));
    }

}
