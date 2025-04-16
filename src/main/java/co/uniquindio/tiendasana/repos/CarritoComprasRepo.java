package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import co.uniquindio.tiendasana.utils.CuentaConstantes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class CarritoComprasRepo {

    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = "CarritoCompras"; // nombre de tu hoja en Google Sheets

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CarritoComprasRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<CarritoCompras> obtenerCarritos() throws IOException {
        List<CarritoCompras> carritos=obtenerCarritosSimples();
        asignarDetalles(carritos);
        return carritos;
    }

    public void asignarDetalles(List<CarritoCompras> carritos) throws IOException {
        List<DetalleCarrito> detalles=obtenerDetallesCarrito();
        for (CarritoCompras carritoCompras:carritos) {
            carritoCompras.setProductos(
                    detalles.stream()
                            .filter(detalleCarrito ->
                                    detalleCarrito.getIdCarrito().equals(carritoCompras.getId())
                            )
                            .collect(Collectors.toList())
            );
        }
    }

    public List<CarritoCompras> filtrar (Predicate<CarritoCompras> expresion) throws IOException, ProductoParseException {
        List<CarritoCompras> carritos = obtenerCarritos();
        return carritos.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    public List<CarritoCompras> obtenerCarritosSimples() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaSimples();
        return mapearFilasCarritos(filas);
    }

    private List<List<Object>> obtenerFilasHojaSimples() throws IOException {
        String rango = SHEET_NAME + "!A2:C"; // Asumiendo que usas columnas: Fecha, IDUsuario, Productos
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return respuesta.getValues();
    }

    private List<CarritoCompras> mapearFilasCarritos(List<List<Object>> filas) {
        List<CarritoCompras> carritos = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                CarritoCompras carrito=mapearCarrito(row);
                carritos.add(carrito);
            } catch (Exception e) {
                System.err.println("Error al procesar fila: " + row + "\n" + e.getMessage());
            }
        }
        return carritos;
    }

    public CarritoCompras mapearCarrito(List<Object> row) {
        String id=row.get(0).toString();
        LocalDateTime fecha=LocalDateTime.parse(row.get(1).toString());
        String idUsuario=row.get(2).toString();

        return CarritoCompras.builder()
                .id(id)
                .fecha(fecha)
                .idUsuario(idUsuario)
                .productos(null)
                .build();
    }

    public List<Object> mapearCarritoInverso(CarritoCompras carrito) {
        return Arrays.asList(
                carrito.getId(),
                carrito.getFecha().toString(),
                carrito.getIdUsuario()
        );
    }

    public List<CarritoCompras> filtrarCarritosSimple (Predicate<CarritoCompras> expresion) throws IOException, ProductoParseException {
        List<CarritoCompras> carritos = obtenerCarritosSimples();
        List<CarritoCompras> carritosFiltrados =  carritos.stream()
                .filter(expresion)
                .collect(Collectors.toList());
        asignarDetalles(carritosFiltrados);
        return carritos;
    }

    //-----------DetallesCarrito-----------------------------------------------------------------------

    public List<DetalleCarrito> obtenerDetallesCarrito() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaDetalle();
        return mapearFilasDetallesCarrito(filas);
    }

    private List<List<Object>> obtenerFilasHojaDetalle() throws IOException {
        String rango = SHEET_NAME + "!A2:C"; // Asumiendo que usas columnas: Fecha, IDUsuario, Productos
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return respuesta.getValues();
    }

    private List<DetalleCarrito> mapearFilasDetallesCarrito(List<List<Object>> filas) throws IOException {
        List<DetalleCarrito> detalles = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                DetalleCarrito detalle=mapearDetalleCarrito(row);
                detalles.add(detalle);
            }catch (NumberFormatException e){
                throw new IOException("Error en el parseo del detaalleCarrito en fila "+ row);
            }
        }
        return detalles;
    }

    public DetalleCarrito mapearDetalleCarrito(List<Object> row) {
        String productoId=row.get(0).toString();
        int cantidad=Integer.parseInt(row.get(1).toString());
        float subtotal=Float.parseFloat(row.get(2).toString());
        String idCarrito=row.get(3).toString();
        return DetalleCarrito.builder()
                .productoId(productoId)
                .cantidad(cantidad)
                .subtotal(subtotal)
                .idCarrito(idCarrito)
                .build();
    }

    public List<Object> mapearDetalleCarritoInverso(DetalleCarrito detalle) {
        return Arrays.asList(
                detalle.getProductoId(),
                ""+detalle.getCantidad(),
                ""+(int)detalle.getSubtotal(),
                detalle.getIdCarrito()
        );
    }

    public List<DetalleCarrito> filtrarDetalles (Predicate<DetalleCarrito> expresion) throws IOException {
        List<DetalleCarrito> detalles = obtenerDetallesCarrito();
        return detalles.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    public void guardar(Cuenta cuenta) throws IOException {

        int cuentas=contarCuentasExistintes();
        String range = SHEET_NAME+"!A"+(2+cuentas)+":"+ CuentaConstantes.COL_REGISTRO_FINAL+(2+cuentas);

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


}
