package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.CodigoValidacion;
import co.uniquindio.tiendasana.model.vo.Usuario;
import co.uniquindio.tiendasana.utils.CuentaConstantes;
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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class CuentaRepo {
    /**
     * Variables y constantes para la conexion con Google Sheets
     */
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = CuentaConstantes.HOJA;

    /**
     * contructor del repositorio donde se le inyecta la dependencia de la hoja
     * @param sheetsService
     */
    public CuentaRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    /**
     * Optiene todos las cuentas
     * @return Cuentas
     * @throws IOException Error al acceder a la base de datos
     */
    public List<Cuenta> obtenerCuentas() throws IOException {
        List<List<Object>> filas = obtenerFilasHoja();
        return mapearFilasCuentas(filas);
    }

    /**
     * Obtiene las cuenats de la base de dato
     * @return Datos obtenidos de la base de datos
     * @throws IOException Error al obtener los datos de la pbase de datos
     */
    private List<List<Object>> obtenerFilasHoja() throws IOException {
        String rango = SHEET_NAME + "!A2:"+CuentaConstantes.COL_REGISTRO_FINAL; // Ajusta según columnas
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Convierte los datos obtenidos desde la base de datos al formato de java
     * @param filas Datos de la base de datos
     * @return Lista de datos en formato de la respectiva clase de java
     */
    private List<Cuenta> mapearFilasCuentas(List<List<Object>> filas) {
        List<Cuenta> cuentas = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                Cuenta cuenta=mapearCuenta(row);
                cuentas.add(cuenta);
            } catch (Exception e) {
                System.err.println("Error al procesar fila: " + row + "\n" + e.getMessage());
            }
        }
        return cuentas;
    }

    /**
     * Cuenta el total de registros de cuentas en la base de datos
     * @return Cantidad de cuenats
     * @throws IOException Error al consultar la base de datos
     */
    public int contarCuentasExistintes() throws IOException {
        String rango = CuentaConstantes.CANT_CUENTAS; // Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    /**
     * Mapea la cuenta a partir de los datos de la base de datos
     * @param row Datos en el formato de la base de datos
     * @return Datos en el formato de las clases de java
     */
    public Cuenta mapearCuenta(List<Object> row) {
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

        return Cuenta.builder()
                .usuario(usuario)
                .email(email)
                .contrasenia(contrasenia)
                .rol(rol)
                .estado(estado)
                .fechaRegistro(fechaRegistro)
                .codigoValidacionContrasenia(codigoValidacionContrasenia)
                .codigoValidacionRegistro(codigoValidacionRegistro)
                .build();
    }

    /**
     * Convierte los datos de la cuenta al formato de la base de datos
     * @param cuenta Datos de la cuenta
     * @return datos en formato de la base de datos
     */
    public List<Object> mapearCuentaInverso(Cuenta cuenta) {
        Usuario  usuario=cuenta.getUsuario();
        CodigoValidacion codigoValidacionRegistro=cuenta.getCodigoValidacionRegistro();
        CodigoValidacion codigoValidacionContrasenia=cuenta.getCodigoValidacionContrasenia();
        return Arrays.asList(
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
                //Esto no se crea en el registro pero se puede crear uno provisional, aunque se debe de cambiar
                //Por cada vez que se desee cambiar la contraseña
                codigoValidacionContrasenia.getCodigo(),
                codigoValidacionContrasenia.getFechaCreacion().toString()
        );
    }

    /**
     * Guarda los datos de la cuenta
     * @param cuenta Cuneta
     * @throws IOException Error al acceder a la base de datos
     */
    public void guardar(Cuenta cuenta) throws IOException {

        int cuentas=contarCuentasExistintes();
        String range = SHEET_NAME+"!A"+(2+cuentas)+":"+CuentaConstantes.COL_REGISTRO_FINAL+(2+cuentas);

        List<List<Object>> values = Arrays.asList(
                mapearCuentaInverso(cuenta)
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
    }

    /**
     * Filtra los las cuentas
     * @param expresion Operacion de filtrado
     * @return Datos filtrados
     * @throws IOException Error al acceder a la base de datos
     */
    public List<Cuenta> filtrar (Predicate<Cuenta> expresion) throws IOException {
        List<Cuenta> cuentas = obtenerCuentas();
        return cuentas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el indice o posicion de una cuenta en la base de datos
     * @param email Email de la cuenta
     * @return Indice donde se encuentra el registro respectivo
     */
    public int obtenerIndiceCuenta(String email) {
        List<Cuenta> cuentas = null;
        int filaCuenta=-1;
        try {
            cuentas = obtenerCuentas();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        int tam=cuentas.size();
        for (int i=0;i<tam;i++) {
            if (cuentas.get(i).getEmail().equals(email)) {
                filaCuenta=i;
                break;
            }
        }
        return filaCuenta;
    }

    /**
     * Actualiza los datos de la base de datos de las cuentas
     * @param cuenta Datos de la cuenta
     * @throws IOException Error al acceder a la base de datos
     */
    public void actualizar(Cuenta cuenta) throws IOException {
        int indice=obtenerIndiceCuenta(cuenta.getEmail());
        if (indice!=-1) {
            String range = SHEET_NAME+"!A"+(2+indice)+":"+CuentaConstantes.COL_REGISTRO_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearCuentaInverso(cuenta)
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
     * Obtiene una cuenta por medio de su dni
     * @param dni DNI o cedula del propietario de la cuenta
     * @return Cuenta obtenida
     * @throws IOException Error al acceder a la base de datos
     * o mas de una cuenta tiene el dni indicado
     */
    public Optional<Cuenta> obtenerPorDNI(String dni) throws IOException {
        List<Cuenta> cuentasObtenidas=
                filtrar(cuenta -> cuenta.getUsuario().getDni().equals(dni));
        if (cuentasObtenidas.isEmpty()) {
            return Optional.empty();
        }
        if (cuentasObtenidas.size()>1) {
            throw new IOException("Mas de una cuenta tiene ese dni");
        }
        return Optional.of(cuentasObtenidas.get(0));
    }

    /**
     * Obtiene una cuenta por medio de su email
     * @param email Email de la cuenta
     * @return Cuenta obtenida
     * @throws IOException Error al acceder a la base de datos
     * o mas de una cuenta tiene el email indicado
     */
    public Optional<Cuenta> obtenerPorEmail(String email) throws IOException {
        List<Cuenta> cuentasObtenidas=
                filtrar(cuenta -> cuenta.getEmail().equals(email));
        if (cuentasObtenidas.isEmpty()) {
            return Optional.empty();
        }
        if (cuentasObtenidas.size()>1) {
            throw new IOException("Mas de una cuenta tiene ese email");
        }
        return Optional.of(cuentasObtenidas.get(0));
    }

    /**
     * Obtiene una cuenta por medio de su email o dni
     * @param dni DNI o cedula del propietario de la cuenta
     * @param email Email de la cuenta
     * @return Cuentas obtenidas
     * @throws IOException Error al acceder a la base de datos
     */
    public List<Cuenta> obtenerPorDniOEmail(String dni, String email) throws IOException {
        return filtrar(cuenta ->
            cuenta.getUsuario().getDni().equals(dni) || cuenta.getEmail().equals(email)
        );
    }

}
