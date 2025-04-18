package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.VentaProducto;
import co.uniquindio.tiendasana.model.vo.DetalleVentaProducto;
import co.uniquindio.tiendasana.model.vo.Pago;
import co.uniquindio.tiendasana.utils.VentaProductoConstantes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class VentaProductoRepo {
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private final String SHEET_NAME = VentaProductoConstantes.HOJA_VENTA;
    private final String SHEET_NAME_DETALLE = VentaProductoConstantes.HOJA_DETALLE;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public VentaProductoRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    /**
     * Optiene todos las ventas de los productos con sus respectivos detalles
     * @return Ventas de los productos
     * @throws IOException
     */
    public List<VentaProducto> obtenerVentas() throws IOException {
        List<VentaProducto> ventas= obtenerVentasSimples();
        asignarDetalles(ventas);
        return ventas;
    }

    public void asignarDetalles(List<VentaProducto> ventas) throws IOException {
        List<DetalleVentaProducto> detalles= obtenerDetallesVenta();
        for (VentaProducto ventaProducto:ventas) {
            ventaProducto.setProductos(
                    detalles.stream()
                            .filter(detalleVenta ->
                                    detalleVenta.getVentaId().equals(ventaProducto.getId())
                            )
                    .collect(Collectors.toList())
            );
        }
    }

    /**
     * Filtra las ventas teniendo en cuenta sus detalles, puede llegar a darse en
     * O(<span style="color:red;">n</span>*<span style="color:blue;">m</span>)<br>
     * <span style="color:red;">n</span> siendo la cantidad total de ventas de productos<br>
     * <span style="color:blue;">m</span> siendo la cantidad total de detalles de todos las ventas juntas
     * @param expresion
     * @return
     * @throws IOException
     * @throws ProductoParseException
     */
    public List<VentaProducto> filtrar (Predicate<VentaProducto> expresion) throws IOException, ProductoParseException {
        List<VentaProducto> carritos = obtenerVentas();
        return carritos.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    /**
     * Optiene todos las ventas de productos sin sus respectivos detalles
     * @return Ventas de productos sin detalles
     * @throws IOException
     */
    public List<VentaProducto> obtenerVentasSimples() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaSimples();
        return mapearFilasVentas(filas);
    }

    private List<List<Object>> obtenerFilasHojaSimples() throws IOException {
        String rango = SHEET_NAME + "!A2:"+ VentaProductoConstantes.COL_REGISTRO_VENTA_FINAL;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
    }

    private List<VentaProducto> mapearFilasVentas(List<List<Object>> filas) {
        List<VentaProducto> ventas = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                VentaProducto carrito= mapearVenta(row);
                ventas.add(carrito);
            } catch (Exception e) {
                System.err.println("Error al procesar fila: " + row + "\n" + e.getMessage());
            }
        }
        return ventas;
    }

    /**
     * Mapea la venta de productos a partir de los datos de la base de datos,
     * sin tener en cuenta sus detalles
     * @param row Datos en el formato de la base de datos
     * @return Datos en el formato de las clases de java
     */
    public VentaProducto mapearVenta(List<Object> row) {
        String id=row.get(0).toString();
        String emailUsuario=row.get(1).toString();
        LocalDateTime fecha=LocalDateTime.parse(row.get(2).toString());
        String totalString = row.get(3).toString();
        float total = totalString.matches("\\d+(\\.\\d+)?") ? Float.parseFloat(totalString) : 0.0f;
        String promocionId=row.get(4).toString();
        String codigoPasarela=row.get(5).toString();

        Pago pago = Pago.builder()
                .id(row.get(6).toString())
                .currency(row.get(7).toString())
                .paymentType(row.get(8).toString())
                .statusDetail(row.get(9).toString())
                .authorizationCode(row.get(10).toString())
                .date(LocalDateTime.parse(row.get(11).toString()))
                .transactionValue(Float.parseFloat(row.get(12).toString()))
                .status(row.get(13).toString())
                .build();

        return VentaProducto.builder()
                .id(id)
                .emailUsario(emailUsuario)
                .fecha(fecha)
                .total(total)
                .promocionId(promocionId)
                .codigoPasarela(codigoPasarela)
                .pago(pago)
                .build();
    }

    /**
     * Convierte los datos de venta al formato de la base de datos sin tener en cuenta los detalles
     * @param venta Datos de las ventas
     * @return datos en formato de la base de datos
     */
    public List<Object> mapearVentaInverso(VentaProducto venta) {
        Pago pago = venta.getPago();
        return Arrays.asList(
                venta.getId(),
                venta.getEmailUsario(),
                venta.getFecha().toString(),
                "" + venta.getTotal(),
                venta.getPromocionId(),
                venta.getCodigoPasarela(),
                pago != null ? pago.getId() : "",
                pago != null ? pago.getCurrency() : "",
                pago != null ? pago.getPaymentType() : "",
                pago != null ? pago.getStatusDetail() : "",
                pago != null ? pago.getAuthorizationCode() : "",
                pago != null ? pago.getDate().toString() : "",
                pago != null ? "" + pago.getTransactionValue() : "",
                pago != null ? pago.getStatus() : ""
        );
    }

    /**
     * Filtra las ventas sin tener en cuenta sus detalles, puede llegar a darse en
     * O(<span style="color:red;">n</span>+<span style="color:blue;">m</span>)<br>
     * <span style="color:red;">n</span> siendo la cantidad total de ventas de productos<br>
     * <span style="color:blue;">m</span> siendo la cantidad total de detalles de todos las ventas juntas
     * @param expresion expresion lambda que filtra las ventas
     * @return Ventas de productos
     * @throws IOException
     * @throws ProductoParseException
     */
    public List<VentaProducto> filtrarVentasSimple (Predicate<VentaProducto> expresion) throws IOException, ProductoParseException {
        List<VentaProducto> ventas = obtenerVentasSimples();
        List<VentaProducto> ventasFiltradas =  ventas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
        asignarDetalles(ventasFiltradas);
        return ventas;
    }

    public int contarVentasExistentes() throws IOException {
        String rango = VentaProductoConstantes.CANT_VENTAS; // Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    /**
     * Guarda los datos de la venta de productos sin tener en cuenta los detalles
     *
     * @param venta Vneta de productos junto con sus detalles
     * @return
     * @throws IOException
     */
    public VentaProducto guardarVentaProductoSimple(VentaProducto venta) throws IOException {

        int detalles= contarVentasExistentes();
        String range = SHEET_NAME+"!A"+(2+detalles)+":"+ VentaProductoConstantes.COL_REGISTRO_VENTA_FINAL+(2+detalles);

        List<List<Object>> values = Arrays.asList(
                mapearVentaInverso(venta)
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
        return venta;
    }

    /**
     * Guarda los datos de la venta de productos junto con sus detalles
     *
     * @param venta Venta de productos junto con sus detalles
     * @return
     * @throws IOException
     */
    public VentaProducto guardarVentaProducto(VentaProducto venta) throws IOException {
        guardarVentaProductoSimple(venta);
        for (DetalleVentaProducto detalle:venta.getProductos()) {
            guardarDetalle(detalle);
        }
        return venta;
    }

    public int obtenerIndiceVenta(String id) {
        List<VentaProducto> ventas = null;
        int filaCuenta=-1;
        try {
            ventas = obtenerVentasSimples();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        int tam=ventas.size();
        for (int i=0;i<tam;i++) {
            if (ventas.get(i).getId().equals(id)) {
                filaCuenta=i;
                break;
            }
        }
        return filaCuenta;
    }

    /**
     * Actualiza los datos de la base de datos de la venta de compras teniendo en cuenta sus detalles
     * @param venta Datos de la venta de productos sin detalles
     * @throws IOException
     */
    public void actualizarVentaSimple(VentaProducto venta) throws IOException {
        int indice= obtenerIndiceVenta(venta.getId());
        if (indice!=-1) {
            String range = SHEET_NAME+"!A"+(2+indice)+":"+ VentaProductoConstantes.COL_REGISTRO_VENTA_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearVentaInverso(venta)
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
     * Actualiza los datos de la base de datos de la venta de productos teniendo en cuenta sus detalles
     * @param venta Datos de la venta de productos
     * @throws IOException
     */
    public void actualizarVenta(VentaProducto venta) throws IOException {
        actualizarVentaSimple(venta);
        List<DetalleVentaProducto> detallesActualizarVenta=new ArrayList<>(venta.getProductos());
        detallesActualizarVenta.retainAll(obtenerDetallesVenta());
        List<DetalleVentaProducto> detallesNuevosVenta=new ArrayList<>(venta.getProductos());
        detallesNuevosVenta.removeAll(detallesActualizarVenta);
        for (DetalleVentaProducto detalle:detallesNuevosVenta) {
            guardarDetalle(detalle);
        }
        for (DetalleVentaProducto detalle:detallesActualizarVenta) {
            actualizarDetalle(detalle);
        }
    }

    //-----------DetallesCarrito-----------------------------------------------------------------------

    /**
     * Optiene todos los los detalles de todos las ventas de productos
     * @return Detalles de ventas de productos
     * @throws IOException
     */
    public List<DetalleVentaProducto> obtenerDetallesVenta() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaDetalle();
        return mapearFilasDetallesVenta(filas);
    }

    private List<List<Object>> obtenerFilasHojaDetalle() throws IOException {
        String rango = SHEET_NAME_DETALLE + "!A2:"+VentaProductoConstantes.COL_REGISTRO_DETALLE_FINAL; // Asumiendo que usas columnas: Fecha, IDUsuario, Productos
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return respuesta.getValues();
    }

    private List<DetalleVentaProducto> mapearFilasDetallesVenta(List<List<Object>> filas) throws IOException {
        List<DetalleVentaProducto> detalles = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                DetalleVentaProducto detalle= mapearDetalleVenta(row);
                detalles.add(detalle);
            }catch (NumberFormatException e){
                throw new IOException("Error en el parseo del detaalleCarrito en fila "+ row);
            }
        }
        return detalles;
    }

    /**
     * Mapea el detalle de una venta de productos a partir de los datos de la base de datos
     * @param row Datos en el formato de la base de datos
     * @return Datos en el formato de las clases de java
     */
    public DetalleVentaProducto mapearDetalleVenta(List<Object> row) {
        String productoId=row.get(0).toString();
        int cantidad=Integer.parseInt(row.get(1).toString());
        float valor=Float.parseFloat(row.get(2).toString());
        String idVenta=row.get(3).toString();
        return DetalleVentaProducto.builder()
                .productoId(productoId)
                .cantidad(cantidad)
                .valor(valor)
                .ventaId(idVenta)
                .build();
    }

    /**
     * Convierte los datos de detalle al formato de la base de datos
     * @param detalle Datos de los detalles
     * @return datos en formato de la base de datos
     */
    public List<Object> mapearDetalleVentaInverso(DetalleVentaProducto detalle) {
        return Arrays.asList(
                detalle.getProductoId(),
                ""+detalle.getCantidad(),
                ""+(int)detalle.getValor(),
                detalle.getVentaId()
        );
    }

    public List<DetalleVentaProducto> filtrarDetalles (Predicate<DetalleVentaProducto> expresion) throws IOException {
        List<DetalleVentaProducto> detalles = obtenerDetallesVenta();
        return detalles.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    public int contarDetallesExistintes() throws IOException {
        String rango = VentaProductoConstantes.CANT_DETALLES;// Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    public void guardarDetalle(DetalleVentaProducto detalle) throws IOException {

        int detalles=contarDetallesExistintes();
        String range = SHEET_NAME_DETALLE+"!A"+(2+detalles)+":"+ VentaProductoConstantes.COL_REGISTRO_DETALLE_FINAL+(2+detalles);

        List<List<Object>> values = Arrays.asList(
                mapearDetalleVentaInverso(detalle)
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
    }

    public int obtenerIndiceDetalle(String idCarrito,String productoId) {
        List<DetalleVentaProducto> detalles = null;
        int filaCuenta=-1;
        try {
            detalles = obtenerDetallesVenta();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        int tam=detalles.size();
        for (int i=0;i<tam;i++) {
            if (detalles.get(i).getProductoId().equals(productoId) &&
                    detalles.get(i).getProductoId().equals(idCarrito)) {
                filaCuenta=i;
                break;
            }
        }
        return filaCuenta;
    }

    public void actualizarDetalle(DetalleVentaProducto detalle) throws IOException {
        int indice=obtenerIndiceDetalle(detalle.getVentaId(),detalle.getProductoId());
        if (indice!=-1) {
            String range = SHEET_NAME_DETALLE+"!A"+(2+indice)+":"+ VentaProductoConstantes.COL_REGISTRO_DETALLE_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearDetalleVentaInverso(detalle)
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

    public List<Object> mapearBorrado() {
        return Arrays.asList(
                "-",
                "0",
                "0",
                "-"
        );
    }

    public void eliminarDetalle(DetalleVentaProducto detalle) throws IOException {
        int indice=obtenerIndiceDetalle(detalle.getVentaId(),detalle.getProductoId());
        if (indice!=-1) {
            String range = SHEET_NAME_DETALLE+"!A"+(2+indice)+":"+ VentaProductoConstantes.COL_REGISTRO_DETALLE_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearBorrado()
            );

            ValueRange body = new ValueRange().setValues(values);

            UpdateValuesResponse result = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW") // "RAW" para insertar como está
                    .execute();

            System.out.println("Numero de celdas eliminadas: " + result.getUpdatedCells());
        } else {
            throw new IOException("Registro no encontrado");
        }
    }

}
