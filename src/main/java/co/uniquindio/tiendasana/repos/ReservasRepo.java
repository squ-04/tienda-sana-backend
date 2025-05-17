package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.documents.Reserva;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.EstadoReserva;
import co.uniquindio.tiendasana.model.vo.Pago;
import co.uniquindio.tiendasana.utils.ReservaConstantes;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class ReservasRepo {
    private final Sheets sheetsService;

    @Value("1rQGF-AGG-kXx1dtS0Va2CQkzHYHXQI8OY5r9NDd3PI8")
    private String spreadsheetId;

    private final String SHEET_NAME = ReservaConstantes.HOJA_RESERVA;
    private final String SHEET_NAME_MESA = ReservaConstantes.HOJA_MESA;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReservasRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public Reserva guardarReserva(Reserva reserva) throws IOException {
        guardarReservaSimple(reserva);
        for (Mesa mesa : reserva.getMesas()) {
            guardarMesa(mesa);
        }
        return reserva;
    }

    private void guardarMesa(Mesa mesa) throws IOException {
        int detalles = contarMesasExistentes();
        String range = SHEET_NAME_MESA + "!A" + (2 + detalles) + ":" + ReservaConstantes.COL_REGISTRO_MESA_FINAL + (2 + detalles);

        List<List<Object>> values = Arrays.asList(
                mapearMesasInverso(mesa)
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
    }

    private int contarMesasExistentes() throws IOException {
        String rango = ReservaConstantes.CANT_MESAS;// Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    private List<Object> mapearMesasInverso(Mesa mesa) {
        return Arrays.asList(
                mesa.getNombre(),
                mesa.getEstado(),
                "" + mesa.getCapacidad(),
                mesa.getLocalidad().getLocalidad(),
                "" + (int) mesa.getPrecioReserva(),
                mesa.getImagen(),
                mesa.getIdReserva(),
                mesa.getIdGestorReserva()
        );
    }

    private Reserva guardarReservaSimple(Reserva reserva) throws IOException {
        int detalles = contarReservasExistentes();
        String range = SHEET_NAME + "!A" + (2 + detalles) + ":" + ReservaConstantes.COL_REGISTRO_RESERVA_FINAL + (2 + detalles);

        List<List<Object>> values = Arrays.asList(
                mapearReservaInverso(reserva)
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
        return reserva;
    }

    private List<Object> mapearReservaInverso(Reserva reserva) {
        Pago pago = reserva.getPago();
        return Arrays.asList(
                reserva.getId(),
                reserva.getUsuarioId(),
                reserva.getFechaReserva().toString(),
                "" + reserva.getCantidadPersonas(),
                reserva.getEstadoReserva(),
                reserva.getCodigoPasarela(),
                pago != null ? pago.getId() : "-",
                pago != null ? pago.getCurrency() : "-",
                pago != null ? pago.getPaymentType() : "-",
                pago != null ? pago.getStatusDetail() : "-",
                pago != null ? pago.getAuthorizationCode() : "-",
                pago != null && pago.getDate() != null ? pago.getDate().toString() : "-",
                pago != null ? "" + pago.getTransactionValue() : "0",
                pago != null ? pago.getStatus() : "-"
        );
    }

    private int contarReservasExistentes() throws IOException {
        String rango = ReservaConstantes.CANT_RESERVAS; // Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    public List<Reserva> filtrarReservasSimple(Predicate<Reserva> expresion) throws IOException {
        List<Reserva> reservas = obtenerReservasSimples();

        List<Reserva> reservasFiltradas = reservas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
        asignarMesas(reservasFiltradas);
        return reservasFiltradas;
    }

    private void asignarMesas(List<Reserva> reservasFiltradas) throws IOException {
        List<Mesa> mesasReservadas = obtenerMesas();
        for (Reserva reserva : reservasFiltradas) {
            reserva.setMesas(
                    mesasReservadas.stream()
                            .filter(mesaReservada ->
                                    mesaReservada.getIdReserva().equals(reserva.getId())
                            )
                            .collect(Collectors.toList())
            );

        }
    }

    private List<Mesa> obtenerMesas() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaMesasReservadas();
        return mapearFilasMesasReservadas(filas);
    }

    private List<Mesa> mapearFilasMesasReservadas(List<List<Object>> filas) throws IOException {
        List<Mesa> mesas = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                Mesa detalle = mapearMesa(row);
                mesas.add(detalle);
            } catch (NumberFormatException e) {
                throw new IOException("Error en el parseo de la mesa reservada en fila " + row);
            }
        }
        return mesas;
    }

    private Mesa mapearMesa(List<Object> row) {
        String nombre = row.get(0).toString();
        String estado = row.get(1).toString();
        int capacidad = Integer.parseInt(row.get(2).toString());
        String localidad = row.get(3).toString();
        float precioReserva = Float.parseFloat(row.get(4).toString());
        String imagen = row.get(5).toString();
        String idMesa = row.get(6).toString();
        String idReserva = row.get(7).toString();
        String idGestorReserva = row.get(8).toString();
        return Mesa.builder()
                .id(idMesa)
                .nombre(nombre)
                .estado(EstadoMesa.fromEstado(estado))
                .localidad(localidad)
                .precioReserva(precioReserva)
                .capacidad(capacidad)
                .imagen(imagen)
                .idReserva(idReserva)
                .idGestorReserva(idGestorReserva)
                .build();
    }

    private List<List<Object>> obtenerFilasHojaMesasReservadas() throws IOException {
        String rango = SHEET_NAME_MESA + "!A2:" + ReservaConstantes.COL_REGISTRO_MESA_FINAL;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return respuesta.getValues();
    }

    private List<Reserva> obtenerReservasSimples() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaSimples();
        return mapearFilasReservas(filas);
    }

    private List<Reserva> mapearFilasReservas(List<List<Object>> filas) throws IOException {
        List<Reserva> reservas = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                Reserva reserva = mapearReserva(row);
                reservas.add(reserva);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return reservas;
    }

    private Reserva mapearReserva(List<Object> row) throws IOException {
        String id = row.get(0).toString();
        String usuarioId = row.get(1).toString();
        LocalDateTime fechaReserva = LocalDateTime.parse(row.get(2).toString());
        String valorString = row.get(3).toString();
        float valorReserva = valorString.matches("\\d+(\\.\\d+)?") ? Float.parseFloat(valorString) : 0.0f;
        int cantidadPersonas = Integer.parseInt(row.get(4).toString());
        EstadoReserva estadoReserva = EstadoReserva.fromEstadoReserva(row.get(5).toString());
        String codigoPasarela = row.get(6).toString();

        Pago pago = null;
        if (row.size() > 7 && row.get(7) != null) {
            pago = Pago.builder()
                    .id(row.get(8) != null ? row.get(6).toString() : "-")
                    .currency(row.size() > 8 && row.get(8) != null ? row.get(8).toString() : "-")
                    .paymentType(row.size() > 9 && row.get(9) != null ? row.get(9).toString() : "-")
                    .statusDetail(row.size() > 10 && row.get(10) != null ? row.get(10).toString() : "-")
                    .authorizationCode(row.size() > 11 && row.get(11) != null ? row.get(11).toString() : "-")
                    .date(row.size() > 12 && !row.get(12).toString().equals("-") ? LocalDateTime.parse(row.get(12).toString()) : null)
                    .transactionValue(row.size() > 13 && !row.get(13).toString().equals("-") ? Float.parseFloat(row.get(13).toString()) : 0.0f)
                    .status(row.size() > 14 && row.get(14) != null ? row.get(14).toString() : "-")
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

    private List<List<Object>> obtenerFilasHojaSimples() throws IOException {
            String rango = SHEET_NAME + "!A2:"+ ReservaConstantes.COL_REGISTRO_RESERVA_FINAL;
            ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
            List<List<Object>> valores=respuesta.getValues();
            if (valores!=null) {
                return valores;
            } else {
                return new ArrayList<>();
            }
    }

    public void actualizarReservaSimple(Reserva reservaCancelar) throws IOException {
        int indice= obtenerIndiceReserva(reservaCancelar.getId());
        if (indice!=-1) {
            String range = SHEET_NAME+"!A"+(2+indice)+":"+ ReservaConstantes.COL_REGISTRO_RESERVA_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearReservaInverso(reservaCancelar)
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

    private int obtenerIndiceReserva(String id) {
        List<Reserva> reservas = null;
        int filaCuenta=-1;
        try {
            reservas = obtenerReservasSimples();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        int tam=reservas.size();
        for (int i=0;i<tam;i++) {
            if (reservas.get(i).getId().equals(id)) {
                filaCuenta=i;
                break;
            }
        }
        return filaCuenta;
    }
}
