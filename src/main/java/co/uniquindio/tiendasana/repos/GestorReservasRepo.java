package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.documents.GestorReservas;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.utils.CuentaConstantes;
import co.uniquindio.tiendasana.utils.GestorReservaConstantes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class GestorReservasRepo {
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = GestorReservaConstantes.HOJA_GESTOR;
    private final String SHEET_NAME_MESA = GestorReservaConstantes.HOJA_MESA;// nombre de tu hoja en Google Sheets

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GestorReservasRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }


    /**
     * Metodo obtener las filas de una hoja como lista de gestores de reservas
     * @param hoja, hoja a la cual se le desean extraer las filas
     * @return Lista de gestores de reservas
     * @throws IOException Error al acceder a la base de datos
     */
    public List<GestorReservas> obtenerGetoresReserva(String hoja) throws IOException {
        List<List<Object>> filas = obtenerFilasHoja(hoja);
        return mapearFilasGestoresReserva(filas);
    }

    /**
     * Este metodo sirve para obtener todas las filas de una hoja dada, esto se representa como una lista de listas
     * de objetos, debido a que cada una de las filas se representa como una lista de onjetos
     * @param hoja, hoja a la cual se le extraen las filas en formato Java
     * @return
     * @throws IOException Error al acceder a la base de datos
     */
    private List<List<Object>> obtenerFilasHoja(String hoja) throws IOException {
        String rango = hoja + "!A2:"+ GestorReservaConstantes.COL_REGISTRO_GESTOR_FINAL; // ID, Nombre, Estado, Localidad, PrecioReserva
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Método que cuenta trae la celda que cuenta la cantidad de productos en la hoja destinada a los gestores de reservas que se
     * le van a mostrar al cliente
     * @return int que indica la cantidad de gestores de reservas que hay
     * @throws IOException Error al acceder a la base de datos
     */
    public int contarGestoresReservaExistintes() throws IOException {
        String rango = GestorReservaConstantes.CANT_GESTORES; // Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    /**
     * Metodo usado para hacer un casteo o mapero de los objetos retornados por la
     * hoja a instacias de la clase gestorReservas
     * @param filas Datos de la base de datos
     * @return Lista de datos en formato de la respectiva clase de java
     */
    private List<GestorReservas> mapearFilasGestoresReserva(List<List<Object>> filas) {
        List<GestorReservas> gestoresreservas = new ArrayList<>();
        for (List<Object> row : filas) {
            GestorReservas gestorReserva= mapearGestorReservas(row);
            gestoresreservas.add(gestorReserva);
        }
        return gestoresreservas;
    }

    /**
     * Metodo con el cual una lista de objetos se transforma en un producto (Entidad)
     * @param row Lista de objetos a transformar (Castear) en producto
     * @return Datos en el formato de las clases de java
     */
    public GestorReservas mapearGestorReservas(List<Object> row) {
        String id=row.get(0).toString();
        String fecha=row.get(1).toString();
        String emailUsuario=row.get(2).toString();

        return GestorReservas.builder()
                .id(id)
                .fecha(fecha.equals("-")?null:LocalDateTime.parse(fecha))
                .emailUsuario(emailUsuario)
                .build();
    }

    /**
     * Metodo para mapear un gestor de reservas como Lista de objetos, ya que es este tipo de dato el que la integracion
     * con Google Sheets permite para hacer la escritura sobre la hoja de calculo
     * @param gestorReservas el cual será el gestor de reservas para tranforma en lista de objetos
     * @return Lista de objetos que representa el gestor de reservas
     */
    public List<Object> mapearGestorReservasInverso(GestorReservas gestorReservas) {
        return Arrays.asList(
                gestorReservas.getId(),
                gestorReservas.getFecha().toString(),
                gestorReservas.getEmailUsuario()
        );
    }

    /**
     * Metodo para fitrar dada una expresión de tipo Predictate y una hoja en la cual se debe de filtrar
     * @param expresion Operacion de filtrado
     * @param hoja Hoja de la base de datos
     * @return  Datos filtrados
     * @throws IOException Error al acceder a la base de datos
     */
    public List<GestorReservas> filtrar (Predicate<GestorReservas> expresion, String hoja) throws IOException {
        List<GestorReservas> gestorReservas = obtenerGetoresReserva(hoja);
        return gestorReservas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    /**
     * Metodo para obtener el indice de un producto en una hoja que tiene como parametro
     * @param id Id del gestor de reservas
     * @return int que indica la posicion del gestor de reservas en las filas validas de la hoja de cáclculo
     */
    public int obtenerIndiceProducto(String id) {
        List<GestorReservas> gestoresReservas = null;
        int filaGestor=-1;
        try {
            gestoresReservas = obtenerGetoresReserva(SHEET_NAME);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        int tam=gestoresReservas.size();
        for (int i=0;i<tam;i++) {
            if (gestoresReservas.get(i).getId().equals(id)) {
                filaGestor=i;
                break;
            }
        }
        return filaGestor;
    }



    /**
     * Metodo que busca actualizar una de las filas en la hoja de cálculo de productos que se comparte con Softr,
     * actualiza toda la fila dada la nueva información de la entidad gestorReservas
     * @param gestorReservas Producto a actualizar
     * @throws IOException Error al acceder a la base de datos
     */
    public void actualizar(GestorReservas gestorReservas) throws IOException {
        int indice= obtenerIndiceProducto(gestorReservas.getId());
        if (indice!=-1) {
            String range = SHEET_NAME +"!A"+(2+indice)+":"+ GestorReservaConstantes.COL_REGISTRO_MESA_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearGestorReservasInverso(gestorReservas)
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

    /**
     * Guarda los datos del gestor de reservas
     * @param gestorReservas Gestor de reservas
     * @throws IOException Error al acceder a la base de datos
     */
    public void guardar(GestorReservas gestorReservas) throws IOException {

        int cuentas=contarGestoresReservaExistintes();
        String range = SHEET_NAME+"!A"+(2+cuentas)+":"+ GestorReservaConstantes.COL_REGISTRO_GESTOR_FINAL+(2+cuentas);

        List<List<Object>> values = Arrays.asList(
                mapearGestorReservasInverso(gestorReservas)
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
    }

    /**
     * Convierte los datos de gestor de reserva eliminado de manera logica al formato de la base de datos
     * @return Datos en formato de la base de datos
     */
    public List<Object> mapearBorrado() {
        return Arrays.asList(
                "-",
                "-",
                "-"
        );
    }

    public Optional<GestorReservas> obtenerPorEmail(String email) throws IOException {
        List<GestorReservas> mesasObtenidas=
                filtrar(mesa -> mesa.getEmailUsuario().equals(email), SHEET_NAME);
        if (mesasObtenidas.isEmpty()) {
            return Optional.empty();
        }
        if (mesasObtenidas.size()>1) {
            throw new IOException("Mas de una mesa tiene ese email");
        }
        return Optional.of(mesasObtenidas.get(0));
    }

}
