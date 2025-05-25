package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.documents.Reserva;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.EstadoReserva;
import co.uniquindio.tiendasana.model.enums.Localidad; // Asegúrate de importar Localidad
import co.uniquindio.tiendasana.model.vo.Pago;
import co.uniquindio.tiendasana.utils.ReservaConstantes;
// import com.fasterxml.jackson.databind.ObjectMapper; // No se está usando ObjectMapper aquí
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
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class ReservasRepo {
    private final Sheets sheetsService;

    @Value("1rQGF-AGG-kXx1dtS0Va2CQkzHYHXQI8OY5r9NDd3PI8") // Si es un valor fijo, no necesita ${}
    private String spreadsheetId;

    private final String SHEET_NAME = ReservaConstantes.HOJA_RESERVA;
    private final String SHEET_NAME_MESA = ReservaConstantes.HOJA_MESA;

    // private final ObjectMapper objectMapper = new ObjectMapper(); // No se está usando

    public ReservasRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    /**
     * Guarda una reserva y todas sus mesas asociadas.
     * @param reserva La reserva a guardar.
     * @return La reserva guardada.
     * @throws IOException Si ocurre un error durante la comunicación con Google Sheets.
     */
    public Reserva guardarReserva(Reserva reserva) throws IOException {
        if (reserva.getId() == null || reserva.getId().isEmpty() || reserva.getId().equals("-")) {
            reserva.setId(UUID.randomUUID().toString()); // Asegurar que la reserva tenga un ID
        }
        guardarReservaSimple(reserva);

        if (reserva.getMesas() != null) {
            for (Mesa mesa : reserva.getMesas()) {
                if (mesa.getId() == null || mesa.getId().isEmpty() || mesa.getId().equals("-")) {
                    System.err.println("Advertencia: Mesa en reserva " + reserva.getId() + " no tiene un ID propio. Se guardará con el ID de reserva como referencia.");
                }
                guardarMesa(mesa, reserva.getId());
            }
        }
        return reserva;
    }

    /**
     * Guarda una mesa individual asociada a una reserva en la hoja de cálculo.
     * @param mesa La mesa a guardar.
     * @param idReserva El ID de la reserva a la que esta mesa pertenece.
     * @throws IOException Si ocurre un error.
     */
    private void guardarMesa(Mesa mesa, String idReserva) throws IOException {
        int detalles = contarMesasExistentes();
        String range = SHEET_NAME_MESA + "!A" + (2 + detalles) + ":" + ReservaConstantes.COL_REGISTRO_MESA_FINAL + (2 + detalles);

        List<List<Object>> values = Arrays.asList(
                mapearMesasInverso(mesa, idReserva)
        );

        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.println("Guardando mesa en hoja: " + (mesa.getNombre() != null ? mesa.getNombre() : "N/A") + " (ID Mesa: " + (mesa.getId() != null ? mesa.getId() : "N/A") + ") para reserva " + idReserva + ". Celdas actualizadas: " + result.getUpdatedCells());
    }

    /**
     * Cuenta el número de mesas existentes en la hoja de mesas.
     * @return El número de mesas.
     * @throws IOException Si ocurre un error.
     */
    private int contarMesasExistentes() throws IOException {
        // CORRECCIÓN: Usar directamente la constante que ya tiene el formato Hoja!Celda
        String rango = ReservaConstantes.CANT_MESAS; // Ej: MesasReservadas!K1
        ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty() || values.get(0).isEmpty() || values.get(0).get(0) == null) {
            System.err.println("Advertencia: Celda de contador de mesas (" + rango + ") está vacía o no es accesible. Asumiendo 0.");
            return 0;
        }
        try {
            return Integer.parseInt(values.get(0).get(0).toString());
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear contador de mesas, valor: '" + values.get(0).get(0).toString() + "' en celda " + rango + ". Asumiendo 0.");
            return 0;
        }
    }

    /**
     * Mapea un objeto Mesa a una lista de objetos para escribir en Google Sheets.
     * El orden es crucial y debe coincidir con `mapearMesa`.
     * @param mesa El objeto Mesa.
     * @param idReservaParam El ID de la reserva a la que pertenece esta mesa.
     * @return Lista de objetos para la fila.
     */
    private List<Object> mapearMesasInverso(Mesa mesa, String idReservaParam) {
        return Arrays.asList(
                mesa.getNombre() != null ? mesa.getNombre() : "-",
                mesa.getEstado() != null ? mesa.getEstado() : "-",
                "" + mesa.getCapacidad(),
                mesa.getLocalidad() != null ? mesa.getLocalidad().getLocalidad() : "-",
                "" + (int) mesa.getPrecioReserva(),
                mesa.getImagen() != null ? mesa.getImagen() : "-",
                mesa.getId() != null ? mesa.getId() : "-",
                idReservaParam != null ? idReservaParam : "-",
                mesa.getIdGestorReserva() != null ? mesa.getIdGestorReserva() : "-"
        );
    }

    /**
     * Guarda la información simple de una reserva (sin sus mesas) en la hoja de cálculo.
     * @param reserva La reserva a guardar.
     * @return La reserva guardada.
     * @throws IOException Si ocurre un error.
     */
    private Reserva guardarReservaSimple(Reserva reserva) throws IOException {
        int detalles = contarReservasExistentes();
        String range = SHEET_NAME + "!A" + (2 + detalles) + ":" + ReservaConstantes.COL_REGISTRO_RESERVA_FINAL + (2 + detalles);
        System.out.println("Guardando reserva simple ID: " + reserva.getId());
        List<List<Object>> values = Arrays.asList(
                mapearReservaInverso(reserva)
        );

        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.println("Celdas de reserva simple actualizadas: " + result.getUpdatedCells());
        return reserva;
    }

    /**
     * Mapea un objeto Reserva a una lista de objetos para escribir en Google Sheets.
     * @param reserva El objeto Reserva.
     * @return Lista de objetos para la fila.
     */
    private List<Object> mapearReservaInverso(Reserva reserva) {
        Pago pago = reserva.getPago();
        return Arrays.asList(
                reserva.getId() != null ? reserva.getId() : "-",
                reserva.getUsuarioId() != null ? reserva.getUsuarioId() : "-",
                reserva.getFechaReserva() != null ? reserva.getFechaReserva().toString() : "-",
                String.valueOf(reserva.getValorReserva()),
                String.valueOf(reserva.getCantidadPersonas()),
                reserva.getEstadoReserva() != null ? reserva.getEstadoReserva().toString() : "-",
                reserva.getCodigoPasarela() != null ? reserva.getCodigoPasarela() : "-",
                pago != null && pago.getId() != null ? pago.getId() : "-",
                pago != null && pago.getCurrency() != null ? pago.getCurrency() : "-",
                pago != null && pago.getPaymentType() != null ? pago.getPaymentType() : "-",
                pago != null && pago.getStatusDetail() != null ? pago.getStatusDetail() : "-",
                pago != null && pago.getAuthorizationCode() != null ? pago.getAuthorizationCode() : "-",
                pago != null && pago.getDate() != null ? pago.getDate().toString() : "-",
                pago != null ? String.valueOf(pago.getTransactionValue()) : "0.0",
                pago != null && pago.getStatus() != null ? pago.getStatus() : "-"
        );
    }

    /**
     * Cuenta el número de reservas existentes.
     * @return El número de reservas.
     * @throws IOException Si ocurre un error.
     */
    private int contarReservasExistentes() throws IOException {
        // CORRECCIÓN: Usar directamente la constante que ya tiene el formato Hoja!Celda
        String rango = ReservaConstantes.CANT_RESERVAS; // Ej: Reservas!Q1
        ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty() || values.get(0).isEmpty() || values.get(0).get(0) == null) {
            System.err.println("Advertencia: Celda de contador de reservas (" + rango + ") está vacía o no es accesible. Asumiendo 0.");
            return 0;
        }
        try {
            return Integer.parseInt(values.get(0).get(0).toString());
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear contador de reservas, valor: '" + values.get(0).get(0).toString() + "' en celda " + rango + ". Asumiendo 0.");
            return 0;
        }
    }

    /**
     * Filtra las reservas y luego les asigna sus mesas correspondientes.
     * @param expresion El predicado para filtrar las reservas.
     * @return Lista de reservas filtradas con sus mesas.
     * @throws IOException Si ocurre un error.
     */
    public List<Reserva> filtrarReservasSimple(Predicate<Reserva> expresion) throws IOException {
        List<Reserva> reservas = obtenerReservasSimples();
        List<Reserva> reservasFiltradas = reservas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
        asignarMesas(reservasFiltradas);
        return reservasFiltradas;
    }

    /**
     * Asigna a cada reserva de la lista sus mesas correspondientes desde la hoja de mesas.
     * @param reservasConFiltro Lista de reservas a las que se les asignarán mesas.
     * @throws IOException Si ocurre un error.
     */
    private void asignarMesas(List<Reserva> reservasConFiltro) throws IOException {
        if (reservasConFiltro == null || reservasConFiltro.isEmpty()) {
            return;
        }
        List<Mesa> todasLasMesasGuardadas = obtenerMesas();
        if (todasLasMesasGuardadas == null || todasLasMesasGuardadas.isEmpty()) {
            System.out.println("No hay mesas guardadas para asignar.");
            return;
        }

        for (Reserva reserva : reservasConFiltro) {
            if (reserva.getId() == null) continue;

            List<Mesa> mesasDeEstaReserva = todasLasMesasGuardadas.stream()
                    .filter(mesaGuardada -> mesaGuardada.getIdReserva() != null && mesaGuardada.getIdReserva().equals(reserva.getId()))
                    .collect(Collectors.toList());
            reserva.setMesas(mesasDeEstaReserva);
        }
    }

    /**
     * Obtiene todas las mesas guardadas en la hoja de mesas.
     * @return Lista de todas las mesas.
     * @throws IOException Si ocurre un error.
     */
    private List<Mesa> obtenerMesas() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaMesasReservadas();
        return mapearFilasMesasReservadas(filas);
    }

    /**
     * Mapea las filas de la hoja de mesas a objetos Mesa.
     * @param filas Las filas de datos.
     * @return Lista de objetos Mesa.
     */
    private List<Mesa> mapearFilasMesasReservadas(List<List<Object>> filas) {
        List<Mesa> mesas = new ArrayList<>();
        if (filas == null) return mesas;
        for (List<Object> row : filas) {
            if (row == null || row.isEmpty()) continue;
            try {
                Mesa mesa = mapearMesa(row);
                if (mesa != null) {
                    mesas.add(mesa);
                }
            } catch (Exception e) {
                System.err.println("Error al parsear fila de mesa reservada: " + row + " - " + e.getMessage());
            }
        }
        return mesas;
    }

    /**
     * Mapea una fila de datos de la hoja de cálculo a un objeto Mesa.
     * @param row La fila de datos.
     * @return El objeto Mesa mapeado, o null si la fila es inválida.
     */
    private Mesa mapearMesa(List<Object> row) {
        if (row == null || row.size() < 7) {
            System.err.println("Fila de mesa inválida o muy corta: " + row);
            return null;
        }

        String idMesa = (row.get(6) != null && !row.get(6).toString().equals("-")) ? row.get(6).toString() : null;
        if (idMesa == null) {
            System.err.println("ID de Mesa (columna G) es nulo o '-' en la fila: " + row + ". Omitiendo mesa.");
            return null;
        }

        String nombre = (row.get(0) != null) ? row.get(0).toString() : "Mesa sin nombre";

        EstadoMesa estado = EstadoMesa.DISPONIBLE;
        if (row.size() > 1 && row.get(1) != null && !row.get(1).toString().isEmpty() && !row.get(1).toString().equals("-")) {
            try {
                estado = EstadoMesa.fromEstado(row.get(1).toString());
            } catch (IllegalArgumentException e) {
                System.err.println("Valor de EstadoMesa inválido: " + row.get(1).toString() + " para mesa ID " + idMesa + ". Usando DISPONIBLE.");
            }
        }

        int capacidad = (row.size() > 2 && row.get(2) != null && row.get(2).toString().matches("\\d+")) ? Integer.parseInt(row.get(2).toString()) : 0;

        String localidadStr = (row.size() > 3 && row.get(3) != null) ? row.get(3).toString() : "";
        Localidad localidad = null;
        if (!localidadStr.isEmpty() && !localidadStr.equals("-")) {
            try {
                localidad = Localidad.fromLocalidad(localidadStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Valor de Localidad inválido: " + localidadStr + " para mesa ID " + idMesa + ". Dejando nulo.");
            }
        }

        float precioReserva = (row.size() > 4 && row.get(4) != null && row.get(4).toString().matches("-?\\d+(\\.\\d+)?")) ? Float.parseFloat(row.get(4).toString()) : 0.0f;
        String imagen = (row.size() > 5 && row.get(5) != null) ? row.get(5).toString() : "";

        String idReserva = (row.size() > 7 && row.get(7) != null) ? row.get(7).toString() : "-";
        String idGestorReserva = (row.size() > 8 && row.get(8) != null) ? row.get(8).toString() : "-";

        return Mesa.builder()
                .id(idMesa)
                .nombre(nombre)
                .estado(estado)
                .localidad(localidad != null ? localidad.getLocalidad() : localidadStr)
                .precioReserva(precioReserva)
                .capacidad(capacidad)
                .imagen(imagen)
                .idReserva(idReserva)
                .idGestorReserva(idGestorReserva)
                .build();
    }

    /**
     * Obtiene todas las filas de la hoja de mesas reservadas.
     * @return Lista de filas.
     * @throws IOException Si ocurre un error.
     */
    private List<List<Object>> obtenerFilasHojaMesasReservadas() throws IOException {
        String rango = SHEET_NAME_MESA + "!A2:" + ReservaConstantes.COL_REGISTRO_MESA_FINAL;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return (respuesta.getValues() != null) ? respuesta.getValues() : new ArrayList<>();
    }

    /**
     * Obtiene las reservas simples (sin detalles de mesas) de la hoja de cálculo.
     * @return Lista de reservas.
     * @throws IOException Si ocurre un error.
     */
    private List<Reserva> obtenerReservasSimples() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaSimples();
        return mapearFilasReservas(filas);
    }

    /**
     * Mapea las filas de la hoja de reservas a objetos Reserva.
     * @param filas Las filas de datos.
     * @return Lista de objetos Reserva.
     */
    private List<Reserva> mapearFilasReservas(List<List<Object>> filas) {
        List<Reserva> reservas = new ArrayList<>();
        if (filas == null) return reservas;
        for (List<Object> row : filas) {
            if (row == null || row.isEmpty() || row.get(0) == null || row.get(0).toString().isEmpty() || row.get(0).toString().equals("-")) {
                continue;
            }
            try {
                Reserva reserva = mapearReserva(row);
                if (reserva != null) {
                    reservas.add(reserva);
                }
            } catch (Exception e) {
                System.err.println("Error al parsear fila de reserva: " + row + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        return reservas;
    }

    /**
     * Mapea una fila de datos de la hoja de cálculo a un objeto Reserva.
     * @param row La fila de datos.
     * @return El objeto Reserva mapeado, o null si la fila es inválida.
     */
    private Reserva mapearReserva(List<Object> row) {
        if (row == null || row.size() < 7) {
            System.err.println("Fila de reserva inválida o muy corta: " + row);
            return null;
        }

        String id = (row.get(0) != null && !row.get(0).toString().equals("-")) ? row.get(0).toString() : null;
        if (id == null) {
            System.err.println("ID de Reserva (columna A) es nulo o '-' en la fila: " + row + ". Omitiendo reserva.");
            return null;
        }

        String usuarioId = (row.size() > 1 && row.get(1) != null) ? row.get(1).toString() : "-";

        LocalDateTime fechaReserva = null;
        if (row.size() > 2 && row.get(2) != null && !row.get(2).toString().equals("-") && !row.get(2).toString().isEmpty()) {
            try {
                fechaReserva = LocalDateTime.parse(row.get(2).toString());
            } catch (DateTimeParseException e) {
                System.err.println("Error parseando fechaReserva: '" + row.get(2).toString() + "' para reserva ID " + id + ". " + e.getMessage());
            }
        }

        String valorString = (row.size() > 3 && row.get(3) != null) ? row.get(3).toString() : "0.0";
        float valorReserva = (valorString.matches("-?\\d+(\\.\\d+)?")) ? Float.parseFloat(valorString) : 0.0f;

        String cantidadPersonasString = (row.size() > 4 && row.get(4) != null) ? row.get(4).toString() : "0";
        int cantidadPersonas = (cantidadPersonasString.matches("\\d+")) ? Integer.parseInt(cantidadPersonasString) : 0;

        EstadoReserva estadoReserva = EstadoReserva.PENDIENTE;
        if (row.size() > 5 && row.get(5) != null && !row.get(5).toString().isEmpty() && !row.get(5).toString().equals("-")) {
            try {
                estadoReserva = EstadoReserva.fromEstadoReserva(row.get(5).toString());
            } catch (IllegalArgumentException e) {
                System.err.println("Valor de EstadoReserva inválido: '" + row.get(5).toString() + "' para reserva ID " + id + ". Usando PENDIENTE.");
            }
        }

        String codigoPasarela = (row.size() > 6 && row.get(6) != null) ? row.get(6).toString() : "-";

        Pago pago = null;
        if (row.size() > 7 && row.get(7) != null && !row.get(7).toString().equals("-")) {
            pago = Pago.builder()
                    .id(row.get(7).toString())
                    .currency( (row.size() > 8 && row.get(8) != null) ? row.get(8).toString() : "-")
                    .paymentType( (row.size() > 9 && row.get(9) != null) ? row.get(9).toString() : "-")
                    .statusDetail( (row.size() > 10 && row.get(10) != null) ? row.get(10).toString() : "-")
                    .authorizationCode( (row.size() > 11 && row.get(11) != null) ? row.get(11).toString() : "-")
                    .date( (row.size() > 12 && row.get(12) != null && !row.get(12).toString().equals("-") && !row.get(12).toString().isEmpty()) ? (LocalDateTime.parse(row.get(12).toString())) : null)
                    .transactionValue( (row.size() > 13 && row.get(13) != null && row.get(13).toString().matches("-?\\d+(\\.\\d+)?")) ? Float.parseFloat(row.get(13).toString()) : 0.0f)
                    .status( (row.size() > 14 && row.get(14) != null) ? row.get(14).toString() : "-")
                    .build();
        }

        return Reserva.builder()
                .id(id)
                .usuarioId(usuarioId)
                .fechaReserva(fechaReserva)
                .valorReserva(valorReserva)
                .cantidadPersonas(cantidadPersonas)
                .estadoReserva(estadoReserva)
                .codigoPasarela(codigoPasarela)
                .pago(pago)
                .build();
    }

    /**
     * Obtiene todas las filas de la hoja de reservas simples.
     * @return Lista de filas.
     * @throws IOException Si ocurre un error.
     */
    private List<List<Object>> obtenerFilasHojaSimples() throws IOException {
        String rango = SHEET_NAME + "!A2:" + ReservaConstantes.COL_REGISTRO_RESERVA_FINAL;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return (respuesta.getValues() != null) ? respuesta.getValues() : new ArrayList<>();
    }

    /**
     * Actualiza una reserva simple (sin detalles de mesas) en la hoja de cálculo.
     * @param reserva La reserva a actualizar.
     * @throws IOException Si ocurre un error o el registro no se encuentra.
     */
    public void actualizarReservaSimple(Reserva reserva) throws IOException {
        int indice = obtenerIndiceReserva(reserva.getId());
        if (indice != -1) {
            String range = SHEET_NAME + "!A" + (2 + indice) + ":" + ReservaConstantes.COL_REGISTRO_RESERVA_FINAL + (2 + indice);
            List<List<Object>> values = Arrays.asList(
                    mapearReservaInverso(reserva)
            );

            ValueRange body = new ValueRange().setValues(values);
            UpdateValuesResponse result = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();
            System.out.println("Celdas de reserva simple actualizadas: " + result.getUpdatedCells());
        } else {
            System.err.println("No se pudo actualizar la reserva simple. Registro no encontrado para ID: " + reserva.getId());
        }
    }

    /**
     * Obtiene el índice de una reserva en la hoja de cálculo.
     * @param id El ID de la reserva.
     * @return El índice (basado en 0 desde la primera fila de datos), o -1 si no se encuentra.
     */
    private int obtenerIndiceReserva(String id) {
        if (id == null || id.isEmpty()) return -1;
        List<Reserva> reservas = null;
        try {
            reservas = obtenerReservasSimples();
        } catch (IOException e) {
            System.err.println("Error al obtener reservas simples para encontrar índice: " + e.getMessage());
            return -1;
        }
        if (reservas == null) return -1;

        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i) != null && id.equals(reservas.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }
}
