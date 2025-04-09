package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.Usuario;
import org.springframework.stereotype.Repository;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CuentaRepo {

    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = "Cuentas";

    public CuentaRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<Cuenta> obtenerCuentas() throws IOException {
        List<List<Object>> filas = obtenerFilasHoja();
        return mapearFilasCuentas(filas);
    }

    private List<List<Object>> obtenerFilasHoja() throws IOException {
        String rango = SHEET_NAME + "!A2:H"; // Ajusta según columnas
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return respuesta.getValues();
    }

    private List<Cuenta> mapearFilasCuentas(List<List<Object>> filas) {
        List<Cuenta> cuentas = new ArrayList<>();

        for (List<Object> row : filas) {
            try {
                Usuario usuario = Usuario.builder()
                        .nombre(row.get(0).toString())
                        .telefono(row.get(1).toString())
                        .direccion(row.get(2).toString())
                        .build();

                String email = row.get(3).toString();
                String contrasenia = row.get(4).toString();
                Rol rol = Rol.valueOf(row.get(5).toString().toUpperCase());
                EstadoCuenta estado = EstadoCuenta.valueOf(row.get(6).toString().toUpperCase());
                LocalDateTime fechaRegistro = LocalDateTime.parse(row.get(7).toString());

                Cuenta cuenta = Cuenta.builder()
                        .usuario(usuario)
                        .email(email)
                        .contrasenia(contrasenia)
                        .rol(rol)
                        .estado(estado)
                        .fechaRegistro(fechaRegistro)
                        .build();

                cuentas.add(cuenta);

            } catch (Exception e) {
                System.err.println("Error al procesar fila: " + row + "\n" + e.getMessage());
            }
        }

        return cuentas;
    }
}
