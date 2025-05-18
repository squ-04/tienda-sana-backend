package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.utils.CarritoConstantes;
import co.uniquindio.tiendasana.utils.GestorReservaConstantes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

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
}
