package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.GestorReservas;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.utils.GestorReservaConstantes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class GestorReservasRepo {
    private final Sheets sheetsService;
    private final MesaRepo mesaRepo;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME_GESTOR = GestorReservaConstantes.HOJA_GESTOR;
    private final String SHEET_NAME_MESAS_GESTOR = GestorReservaConstantes.HOJA_MESA;// nombre de tu hoja en Google Sheets

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GestorReservasRepo(Sheets sheetsService,MesaRepo mesaRepo) {
        this.sheetsService = sheetsService;
        this.mesaRepo = mesaRepo;
    }


    /**
     * Metodo obtener las filas de una hoja como lista de gestores de reservas
     * @return Lista de gestores de reservas
     * @throws IOException Error al acceder a la base de datos
     */
    public List<GestorReservas> obtenerGetoresReserva() throws IOException {
        List<List<Object>> filas = obtenerFilasHoja(SHEET_NAME_GESTOR, GestorReservaConstantes.COL_REGISTRO_GESTOR_FINAL);
        return mapearFilasGestoresReserva(filas);
    }

    /**
     * Este metodo sirve para obtener todas las filas de una hoja dada, esto se representa como una lista de listas
     * de objetos, debido a que cada una de las filas se representa como una lista de onjetos
     * @param hoja, hoja a la cual se le extraen las filas en formato Java
     * @return
     * @throws IOException Error al acceder a la base de datos
     */
    private List<List<Object>> obtenerFilasHoja(String hoja, String columnaFinal) throws IOException {
        String rango = hoja + "!A2:" + columnaFinal;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores = respuesta.getValues();
        return (valores != null) ? valores : new ArrayList<>();
    }

    /**
     * Método que cuenta trae la celda que cuenta la cantidad de productos en la hoja destinada a los gestores de reservas que se
     * le van a mostrar al cliente
     * @return int que indica la cantidad de gestores de reservas que hay
     * @throws IOException Error al acceder a la base de datos
     */
    public int contarGestoresReservaExistintes() throws IOException {
        // CORRECCIÓN: Usar directamente la constante que ya tiene el formato Hoja!Celda
        String rango = GestorReservaConstantes.CANT_GESTORES; // Ej: GestorReservas!F1
        ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty() || values.get(0).isEmpty() || values.get(0).get(0) == null) {
            System.err.println("Advertencia: Celda de contador de gestores (" + rango + ") está vacía o no es accesible. Asumiendo 0.");
            return 0;
        }
        try {
            return Integer.parseInt(values.get(0).get(0).toString());
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear contador de gestores, valor: '" + values.get(0).get(0).toString() + "' en celda " + rango + ". Asumiendo 0.");
            return 0;
        }
    }

    /**
     * Cuenta las mesas existentes en la hoja de mesas del gestor (MesasAReservar).
     * @return El número de mesas.
     * @throws IOException Si ocurre un error.
     */
    public int contarMesasEnHojaGestor() throws IOException {
        // CORRECCIÓN: Usar directamente la constante que ya tiene el formato Hoja!Celda
        String rango = GestorReservaConstantes.CANT_MESAS; // Ej: MesasAReservar!K1
        ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty() || values.get(0).isEmpty() || values.get(0).get(0) == null) {
            System.err.println("Advertencia: Celda de contador de mesas del gestor (" + rango + ") está vacía o no es accesible. Asumiendo 0.");
            return 0;
        }
        try {
            return Integer.parseInt(values.get(0).get(0).toString());
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear contador de mesas del gestor, valor: '" + values.get(0).get(0).toString() + "' en celda " + rango + ". Asumiendo 0.");
            return 0;
        }
    }

    /**
     * Mapea filas de datos a objetos GestorReservas.
     * @param filas Lista de filas de la hoja.
     * @return Lista de GestorReservas.
     */
    private List<GestorReservas> mapearFilasGestoresReserva(List<List<Object>> filas) {
        List<GestorReservas> gestoresreservas = new ArrayList<>();
        if (filas == null) return gestoresreservas;
        for (List<Object> row : filas) {
            if (row == null || row.isEmpty() || row.get(0) == null || row.get(0).toString().isEmpty() || row.get(0).toString().equals("-")) {
                continue;
            }
            try {
                GestorReservas gestorReserva = mapearGestorReservas(row);
                if (gestorReserva != null) {
                    gestoresreservas.add(gestorReserva);
                }
            } catch (Exception e) {
                System.err.println("Error al parsear fila de GestorReservas: " + row + " - " + e.getMessage());
            }
        }
        return gestoresreservas;
    }

    /**
     * Metodo con el cual una lista de objetos se transforma en un producto (Entidad)
     * @param row Lista de objetos a transformar (Castear) en producto
     * @return Datos en el formato de las clases de java
     */
    public GestorReservas mapearGestorReservas(List<Object> row) {
        if (row == null || row.size() < 3) {
            System.err.println("Fila de GestorReservas inválida o muy corta: " + row);
            return null;
        }
        String id = row.get(0).toString();
        if (id.equals("-")) return null; // Si el ID es "-", consideramos la fila vacía/inválida

        String fechaStr = row.get(1).toString();
        LocalDateTime fecha = null;
        if (!fechaStr.equals("-") && !fechaStr.isEmpty()) {
            try {
                fecha = LocalDateTime.parse(fechaStr);
            } catch (DateTimeParseException e) {
                System.err.println("Error parseando fecha para GestorReservas ID " + id + ": " + fechaStr);
            }
        }
        String emailUsuario = row.get(2).toString();

        return GestorReservas.builder()
                .id(id)
                .fecha(fecha)
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
                gestorReservas.getId() != null ? gestorReservas.getId() : "-",
                gestorReservas.getFecha() != null ? gestorReservas.getFecha().toString() : "-",
                gestorReservas.getEmailUsuario() != null ? gestorReservas.getEmailUsuario() : "-"
        );
    }

    /**
     * Mapea un objeto Mesa y el ID del gestor a una lista de objetos para escribir en la hoja de MesasAReservar.
     * El orden debe coincidir con las columnas de la imagen:
     * A:Nombre, B:Estado, C:Capacidad, D:Localidad, E:precioReserva, F:Imagen, G:ID_Mesa, H:idReserva, I:idGestorReserva
     * @param mesa El objeto Mesa.
     * @param idGestor El ID del GestorReservas al que esta mesa está vinculada.
     * @return Lista de objetos para la fila.
     */
    public List<Object> mapearMesaParaHojaGestorInverso(Mesa mesa, String idGestor) {
        return Arrays.asList(
                mesa.getNombre() != null ? mesa.getNombre() : "-",                                  // A: Nombre
                mesa.getEstado() != null ? mesa.getEstado() : "-",                                  // B: Estado
                String.valueOf(mesa.getCapacidad()),                                                // C: Capacidad
                mesa.getLocalidad() != null ? mesa.getLocalidad().getLocalidad() : "-",             // D: Localidad
                String.valueOf((int)mesa.getPrecioReserva()),                                       // E: precioReserva
                mesa.getImagen() != null ? mesa.getImagen() : "-",                                  // F: Imagen referencial
                mesa.getId() != null ? mesa.getId() : "-",                                          // G: Softr Record ID (ID de la Mesa)
                mesa.getIdReserva() != null ? mesa.getIdReserva() : "-",                            // H: idReserva (puede ser "-" si aún no está en una reserva activa)
                idGestor != null ? idGestor : "-"                                                   // I: idGestorReserva
        );
    }

    /**
     * Guarda una mesa en la hoja de "MesasAReservar" asociada a un GestorReservas.
     * @param mesa La mesa a guardar.
     * @param idGestor El ID del GestorReservas.
     * @throws IOException Si ocurre un error.
     */
    public void guardarMesaEnHojaGestor(Mesa mesa, String idGestor) throws IOException {
        int proximaFila = contarMesasEnHojaGestor() + 2; // +2 porque la data empieza en la fila 2
        // COL_REGISTRO_MESA_FINAL de GestorReservaConstantes debe ser "I"
        String range = SHEET_NAME_MESAS_GESTOR+ "!A" + proximaFila + ":" + GestorReservaConstantes.COL_REGISTRO_MESA_FINAL + proximaFila;

        List<List<Object>> values = Arrays.asList(
                mapearMesaParaHojaGestorInverso(mesa, idGestor)
        );

        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.println("Mesa '" + mesa.getNombre() + "' guardada en " + SHEET_NAME_MESAS_GESTOR + " para gestor " + idGestor + ". Celdas actualizadas: " + result.getUpdatedCells());
    }

    /**
     * Metodo para fitrar dada una expresión de tipo Predictate y una hoja en la cual se debe de filtrar
     * @param expresion Operacion de filtrado
     * @return  Datos filtrados
     * @throws IOException Error al acceder a la base de datos
     */
    public List<GestorReservas> filtrar(Predicate<GestorReservas> expresion) throws IOException {
        List<GestorReservas> gestores = obtenerGetoresReserva();
        List<GestorReservas> gestoresFiltrados = gestores.stream()
                .filter(expresion)
                .collect(Collectors.toList());
        for (GestorReservas gestor : gestoresFiltrados) {
            if (gestor != null && gestor.getId() != null) {
                asignarMesas(gestor);
            }
        }
        return gestoresFiltrados;
    }

    private void asignarMesas(GestorReservas gestor) throws IOException {
        List<Mesa> mesas = mesaRepo.obtenerPorGestorReserva(gestor.getId(), SHEET_NAME_MESAS_GESTOR);
        gestor.setMesas(mesas);
    }

    /**
     * Metodo para obtener el indice de un producto en una hoja que tiene como parametro
     * @param id Id del gestor de reservas
     * @return int que indica la posicion del gestor de reservas en las filas validas de la hoja de cáclculo
     */
    public int obtenerIndiceGestor(String id) {
        if (id == null || id.isEmpty()) return -1;
        List<GestorReservas> gestoresReservas = null;
        try {
            gestoresReservas = obtenerGetoresReserva();
        } catch (IOException e) {
            System.err.println("Error al obtener gestores para encontrar índice: " + e.getMessage());
            return -1;
        }
        if (gestoresReservas == null) return -1;

        for (int i = 0; i < gestoresReservas.size(); i++) {
            if (gestoresReservas.get(i) != null && id.equals(gestoresReservas.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }



    /**
     * Metodo que busca actualizar una de las filas en la hoja de cálculo de productos que se comparte con Softr,
     * actualiza toda la fila dada la nueva información de la entidad gestorReservas
     * @param gestorReservas Producto a actualizar
     * @throws IOException Error al acceder a la base de datos
     */
    public void actualizar(GestorReservas gestorReservas) throws IOException {
        int indice = obtenerIndiceGestor(gestorReservas.getId());
        if (indice != -1) {
            // CORRECCIÓN: El rango debe usar COL_REGISTRO_GESTOR_FINAL para la hoja de gestores.
            String range = SHEET_NAME_GESTOR + "!A" + (2 + indice) + ":" + GestorReservaConstantes.COL_REGISTRO_GESTOR_FINAL + (2 + indice);
            List<List<Object>> values = Arrays.asList(
                    mapearGestorReservasInverso(gestorReservas)
            );
            ValueRange body = new ValueRange().setValues(values);
            UpdateValuesResponse result = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();
            System.out.println("Gestor de Reservas actualizado. Celdas: " + result.getUpdatedCells());
        } else {
            throw new IOException("Registro de GestorReservas no encontrado para ID: " + gestorReservas.getId());
        }
    }

    /**
     * Guarda los datos del gestor de reservas
     * @param gestorReservas Gestor de reservas
     * @throws IOException Error al acceder a la base de datos
     */
    public void guardar(GestorReservas gestorReservas) throws IOException {
        int proximaFila = contarGestoresReservaExistintes() + 2;
        String range = SHEET_NAME_GESTOR + "!A" + proximaFila + ":" + GestorReservaConstantes.COL_REGISTRO_GESTOR_FINAL + proximaFila;

        List<List<Object>> values = Arrays.asList(
                mapearGestorReservasInverso(gestorReservas)
        );
        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.println("Gestor de Reservas guardado. Celdas: " + result.getUpdatedCells());
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
        if (email == null || email.isEmpty()) return Optional.empty();
        List<GestorReservas> gestoresFiltrados = filtrar(gestor -> gestor != null && email.equals(gestor.getEmailUsuario()));
        if (gestoresFiltrados.isEmpty()) {
            return Optional.empty();
        }
        if (gestoresFiltrados.size() > 1) {
            System.err.println("Advertencia: Múltiples GestorReservas encontrados para el email: " + email);
        }
        return Optional.of(gestoresFiltrados.get(0));
    }

    private List<Mesa> obtenerTodasLasMesasDeHojaGestor() throws IOException {
        List<List<Object>> filas = obtenerFilasHoja(SHEET_NAME_MESAS_GESTOR, GestorReservaConstantes.COL_REGISTRO_MESA_FINAL);
        List<Mesa> mesas = new ArrayList<>();
        if (filas == null) return mesas;
        for (List<Object> row : filas) {
            if (row == null || row.isEmpty()) continue;
            try {
                Mesa mesa = mapearMesaDesdeHojaGestor(row); // Necesitas este método
                if (mesa != null && mesa.getId() != null && !mesa.getId().equals("-")) {
                    mesas.add(mesa);
                }
            } catch (Exception e) {
                System.err.println("Error al parsear fila de Mesa desde hoja gestor: " + row + " - " + e.getMessage());
            }
        }
        return mesas;
    }

    // Necesitas un método para mapear una fila de la hoja de mesas del gestor a un objeto Mesa
    // Similar al MesaRepo.mapearMesa, pero adaptado si las columnas o el significado son diferentes.
    // Basado en la imagen, el formato es:
    // A:Nombre, B:Estado, C:Capacidad, D:Localidad, E:precioReserva, F:Imagen, G:ID_Mesa, H:idReserva, I:idGestorReserva
    private Mesa mapearMesaDesdeHojaGestor(List<Object> row) {
        if (row == null || row.size() < 9) { // Se esperan 9 columnas
            System.err.println("Fila de Mesa (hoja gestor) inválida o muy corta: " + row);
            return null;
        }

        String idMesa = (row.get(6) != null && !row.get(6).toString().equals("-")) ? row.get(6).toString() : null;
        if (idMesa == null) {
            System.err.println("ID de Mesa (columna G) es nulo o '-' en hoja gestor. Fila: " + row);
            return null;
        }

        String nombre = (row.get(0) != null) ? row.get(0).toString() : "Mesa " + idMesa.substring(0, Math.min(idMesa.length(), 5));

        EstadoMesa estado = EstadoMesa.DISPONIBLE;
        if (row.get(1) != null && !row.get(1).toString().isEmpty() && !row.get(1).toString().equals("-")) {
            try {
                estado = EstadoMesa.fromEstado(row.get(1).toString());
            } catch (IllegalArgumentException e) {
                System.err.println("EstadoMesa inválido '" + row.get(1).toString() + "' para mesa ID " + idMesa + ". Usando DISPONIBLE.");
            }
        }

        int capacidad = (row.get(2) != null && row.get(2).toString().matches("\\d+")) ? Integer.parseInt(row.get(2).toString()) : 0;

        String localidadStr = (row.get(3) != null) ? row.get(3).toString() : "";
        // La conversión a Enum Localidad se hace en el builder de Mesa

        float precioReserva = (row.get(4) != null && row.get(4).toString().matches("-?\\d*\\.?\\d+")) ? Float.parseFloat(row.get(4).toString()) : 0.0f;
        String imagen = (row.get(5) != null) ? row.get(5).toString() : "";
        String idReserva = (row.get(7) != null) ? row.get(7).toString() : "-";
        String idGestorReserva = (row.get(8) != null) ? row.get(8).toString() : "-";

        return Mesa.builder()
                .id(idMesa)
                .nombre(nombre)
                .estado(estado)
                .localidad(localidadStr) // El builder de Mesa debe manejar la conversión
                .precioReserva(precioReserva)
                .capacidad(capacidad)
                .imagen(imagen)
                .idReserva(idReserva)
                .idGestorReserva(idGestorReserva)
                .build();
    }

    private int obtenerIndiceMesaEnHojaGestor(String idMesa, String idGestor) throws IOException {
        if (idMesa == null || idGestor == null) return -1;
        List<Mesa> mesasDelGestor = obtenerTodasLasMesasDeHojaGestor();
        for (int i = 0; i < mesasDelGestor.size(); i++) {
            Mesa mesa = mesasDelGestor.get(i);
            if (mesa != null && idMesa.equals(mesa.getId()) && idGestor.equals(mesa.getIdGestorReserva())) {
                return i; // Devuelve el índice 0-based relativo a las filas de datos (A2 es 0)
            }
        }
        return -1; // No encontrada
    }

    public void eliminarMesaDeHojaGestor(String idMesa, String idGestor) throws IOException {
        int indiceMesa = obtenerIndiceMesaEnHojaGestor(idMesa, idGestor);

        if (indiceMesa != -1) {
            // El rango es A<fila>:I<fila> para 9 columnas
            String range = SHEET_NAME_MESAS_GESTOR + "!A" + (2 + indiceMesa) + ":" + GestorReservaConstantes.COL_REGISTRO_MESA_FINAL + (2 + indiceMesa);

            // Preparamos una fila con valores "borrados" o placeholders
            // Es importante que la lista tenga la misma cantidad de columnas que la hoja (9 en este caso)
            List<Object> filaBorrada = Arrays.asList(
                    "-", // Nombre
                    "-", // Estado
                    "0", // Capacidad
                    "-", // Localidad
                    "0", // precioReserva
                    "-", // Imagen referencial
                    idMesa, // Mantenemos el ID de la mesa para posible referencia futura, o "-" si se prefiere
                    "-", // idReserva
                    "-"  // idGestorReserva (marcando que ya no está vinculada a ESTE gestor)
            );

            List<List<Object>> values = Arrays.asList(filaBorrada);
            ValueRange body = new ValueRange().setValues(values);

            UpdateValuesResponse result = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();
            System.out.println("Mesa ID " + idMesa + " desvinculada del gestor ID " + idGestor + " en hoja " + SHEET_NAME_MESAS_GESTOR + ". Celdas actualizadas: " + result.getUpdatedCells());
        } else {
            System.err.println("No se pudo encontrar la mesa ID " + idMesa + " asociada al gestor ID " + idGestor + " en la hoja " + SHEET_NAME_MESAS_GESTOR + " para eliminarla/desvincularla.");
            // Podrías lanzar una excepción si es un error crítico no encontrarla
            // throw new IOException("Mesa no encontrada en la hoja del gestor para eliminar.");
        }
    }

}
