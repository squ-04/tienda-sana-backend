package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.documents.Reserva;
import co.uniquindio.tiendasana.model.vo.Pago;
import co.uniquindio.tiendasana.utils.ReservaConstantes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
        for (Mesa mesa:reserva.getMesas()) {
            guardarMesa(mesa);
        }
        return reserva;
    }

    private void guardarMesa(Mesa mesa) throws IOException {
        int detalles=contarMesasExistentes();
        String range = SHEET_NAME_MESA+"!A"+(2+detalles)+":"+ ReservaConstantes.COL_REGISTRO_MESA_FINAL+(2+detalles);

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
                ""+mesa.getCapacidad(),
                mesa.getLocalidad().getLocalidad(),
                ""+(int)mesa.getPrecioReserva(),
                mesa.getImagen(),
                mesa.getIdReserva(),
                mesa.getIdGestorReserva()
        );
    }

    private Reserva guardarReservaSimple(Reserva reserva) throws IOException {
        int detalles= contarReservasExistentes();
        String range = SHEET_NAME+"!A"+(2+detalles)+":"+ ReservaConstantes.COL_REGISTRO_RESERVA_FINAL+(2+detalles);

        List<List<Object>> values = Arrays.asList(
                mapearReservaInverso(reserva )
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
}
