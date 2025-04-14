package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.CodigoValidacion;
import co.uniquindio.tiendasana.model.vo.Usuario;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import org.springframework.stereotype.Repository;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
                        .dni(row.get(0).toString())
                        .nombre(row.get(1).toString())
                        .telefono(row.get(2).toString())
                        .direccion(row.get(3).toString())
                        .build();

                String email = row.get(4).toString();
                String contrasenia = row.get(5).toString();
                Rol rol = Rol.valueOf(row.get(6).toString().toUpperCase());
                EstadoCuenta estado = EstadoCuenta.valueOf(row.get(7).toString().toUpperCase());
                LocalDateTime fechaRegistro = LocalDateTime.parse(row.get(8).toString());

                CodigoValidacion codigoValidacionRegistro= CodigoValidacion.builder()
                        .codigo(row.get(9).toString())
                        .fechaCreacion(LocalDateTime.parse(row.get(10).toString()))
                        .build();

                CodigoValidacion codigoValidacionContrasenia= CodigoValidacion.builder()
                        .codigo(row.get(11).toString())
                        .fechaCreacion(LocalDateTime.parse(row.get(12).toString()))
                        .build();

                Cuenta cuenta = Cuenta.builder()
                        .usuario(usuario)
                        .email(email)
                        .contrasenia(contrasenia)
                        .rol(rol)
                        .estado(estado)
                        .fechaRegistro(fechaRegistro)
                        .codigoValidacionContrasenia(codigoValidacionContrasenia)
                        .codigoValidacionRegistro(codigoValidacionRegistro)
                        .build();

                cuentas.add(cuenta);

            } catch (Exception e) {
                System.err.println("Error al procesar fila: " + row + "\n" + e.getMessage());
            }
        }

        return cuentas;
    }

    public int contarCuentasExistintes() throws IOException {
        String rango = SHEET_NAME + "!P2:P"; // Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    public void ingresarCuenta(Cuenta cuenta) throws IOException {

        int cuentas=contarCuentasExistintes();
        String range = SHEET_NAME+"!A"+(2+cuentas)+":M"+(2+cuentas);

        Usuario  usuario=cuenta.getUsuario();
        CodigoValidacion codigoValidacionRegistro=cuenta.getCodigoValidacionRegistro();
        CodigoValidacion codigoValidacionContrasenia=cuenta.getCodigoValidacionContrasenia();

        List<List<Object>> values = Arrays.asList(
                Arrays.asList(
                        usuario.getDni(),
                        usuario.getNombre(),
                        usuario.getTelefono(),
                        usuario.getDireccion(),
                        cuenta.getEmail(),
                        cuenta.getContrasenia(),
                        cuenta.getRol().toString(),
                        cuenta.getEstado().toString(),
                        cuenta.getFechaRegistro().toString(),
                        codigoValidacionRegistro.getCodigo(),
                        codigoValidacionRegistro.getFechaCreacion().toString(),
                        codigoValidacionContrasenia.getCodigo(),
                        codigoValidacionContrasenia.getFechaCreacion().toString()
                )
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Número de celdas actualizadas: " + result.getUpdatedCells());

    }

}
