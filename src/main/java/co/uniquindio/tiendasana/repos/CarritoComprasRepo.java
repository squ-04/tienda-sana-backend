package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import co.uniquindio.tiendasana.utils.CarritoConstantes;
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

    private final String SHEET_NAME = CarritoConstantes.HOJA_CARRITO;
    private final String SHEET_NAME_DETALLE = CarritoConstantes.HOJA_DETALLE;// nombre de tu hoja en Google Sheets

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CarritoComprasRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    /**
     * Optiene todos los carritos de compras con sus respectivos detalles
     * @return Carritos de compras
     * @throws IOException
     */
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

    /**
     * Filtra los carritos teniendo en cuenta sus detalles, puede llegar a darse en
     * O(<span style="color:red;">n</span>*<span style="color:blue;">m</span>)<br>
     * <span style="color:red;">n</span> siendo la cantidad total de carritos de compra<br>
     * <span style="color:blue;">m</span> siendo la cantidad total de detalles de todos los carritos juntos
     * @param expresion
     * @return
     * @throws IOException
     */
    public List<CarritoCompras> filtrar (Predicate<CarritoCompras> expresion) throws IOException {
        List<CarritoCompras> carritos = obtenerCarritos();
        return carritos.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    /**
     * Optiene todos los carritos de compras sin sus respectivos detalles
     * @return Carritos de compras sin detalles
     * @throws IOException
     */
    public List<CarritoCompras> obtenerCarritosSimples() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaSimples();
        return mapearFilasCarritos(filas);
    }

    private List<List<Object>> obtenerFilasHojaSimples() throws IOException {
        String rango = SHEET_NAME + "!A2:"+CarritoConstantes.COL_REGISTRO_CARRITO_FINAL;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
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

    /**
     * Mapea el carrito de compra a partir de los datos de la base de datos,
     * sin tener en cuenta sus detalles
     * @param row Datos en el formato de la base de datos
     * @return Datos en el formato de las clases de java
     */
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

    /**
     * Convierte los datos de carrito al formato de la base de datos sin tener en cuenta los detalles
     * @param carrito Datos de los carritos
     * @return datos en formato de la base de datos
     */
    public List<Object> mapearCarritoInverso(CarritoCompras carrito) {
        return Arrays.asList(
                carrito.getId(),
                carrito.getFecha().toString(),
                carrito.getIdUsuario()
        );
    }

    /**
     * Filtra los carritos sin tener en cuenta sus detalles, puede llegar a darse en
     * O(<span style="color:red;">n</span>+<span style="color:blue;">m</span>)<br>
     * <span style="color:red;">n</span> siendo la cantidad total de carritos de compra<br>
     * <span style="color:blue;">m</span> siendo la cantidad total de detalles de todos los carritos juntos
     * @param expresion
     * @return
     * @throws IOException
     * @throws ProductoParseException
     */
    public List<CarritoCompras> filtrarCarritosSimple (Predicate<CarritoCompras> expresion) throws IOException {
        List<CarritoCompras> carritos = obtenerCarritosSimples();
        List<CarritoCompras> carritosFiltrados =  carritos.stream()
                .filter(expresion)
                .collect(Collectors.toList());
        asignarDetalles(carritosFiltrados);
        return carritos;
    }

    public int contarCarritosExistintes() throws IOException {
        String rango = CarritoConstantes.CANT_CARRITOS; // Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    /**
     * Guarda los datos del carrito de compras sin tener en cuenta los detalles
     * @param carrito Carrito de compras junto con sus detalles
     * @throws IOException
     */
    public void guardarCarritoCompraSimple(CarritoCompras carrito) throws IOException {

        int detalles=contarCarritosExistintes();
        String range = SHEET_NAME+"!A"+(2+detalles)+":"+ CarritoConstantes.COL_REGISTRO_CARRITO_FINAL+(2+detalles);

        List<List<Object>> values = Arrays.asList(
                mapearCarritoInverso(carrito)
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
    }

    /**
     * Guarda los datos del carrito de compras junto con sus detalles
     * @param carrito Carrito de compras junto con sus detalles
     * @throws IOException
     */
    public void guardarCarritoCompra(CarritoCompras carrito) throws IOException {
        guardarCarritoCompraSimple(carrito);
        for (DetalleCarrito detalle:carrito.getProductos()) {
            guardarDetalle(detalle);
        }
    }

    public int obtenerIndiceCarrito(String idUsuario) {
        List<CarritoCompras> carritos = null;
        int filaCuenta=-1;
        try {
            carritos = obtenerCarritosSimples();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        int tam=carritos.size();
        for (int i=0;i<tam;i++) {
            if (carritos.get(i).getIdUsuario().equals(idUsuario)) {
                filaCuenta=i;
                break;
            }
        }
        return filaCuenta;
    }

    /**
     * Actualiza los datos de la base de datos del carrito de compras teniendo en cuenta sus detalles
     * @param carrito Datos del carrito de compras sin detalles
     * @throws IOException
     */
    public void actualizarCarritoSimple(CarritoCompras carrito) throws IOException {
        int indice=obtenerIndiceCarrito(carrito.getIdUsuario());
        if (indice!=-1) {
            String range = SHEET_NAME+"!A"+(2+indice)+":"+ CarritoConstantes.COL_REGISTRO_CARRITO_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearCarritoInverso(carrito)
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
     * Actualiza los datos de la base de datos del carrito de compras teniendo en cuenta sus detalles
     * @param carrito Datos del carrito de compras
     * @throws IOException
     */
    public void actualizarCarrito(CarritoCompras carrito) throws IOException {
        actualizarCarritoSimple(carrito);
        List<DetalleCarrito> detallesActualizarCarrito=new ArrayList<>(carrito.getProductos());
        detallesActualizarCarrito.retainAll(obtenerDetallesCarrito());
        List<DetalleCarrito> detallesNuevosCarrito=new ArrayList<>(carrito.getProductos());
        detallesNuevosCarrito.removeAll(detallesActualizarCarrito);
        for (DetalleCarrito detalle:detallesNuevosCarrito) {
            guardarDetalle(detalle);
        }
        for (DetalleCarrito detalle:detallesActualizarCarrito) {
            actualizarDetalle(detalle);
        }
    }

    //-----------DetallesCarrito-----------------------------------------------------------------------

    /**
     * Optiene todos los los detalles de todos los carritos de compras
     * @return Detalles de carritos de compras
     * @throws IOException
     */
    public List<DetalleCarrito> obtenerDetallesCarrito() throws IOException {
        List<List<Object>> filas = obtenerFilasHojaDetalle();
        return mapearFilasDetallesCarrito(filas);
    }

    private List<List<Object>> obtenerFilasHojaDetalle() throws IOException {
        String rango = SHEET_NAME_DETALLE + "!A2:"+CarritoConstantes.COL_REGISTRO_DETALLE_FINAL; // Asumiendo que usas columnas: Fecha, IDUsuario, Productos
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
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

    /**
     * Mapea el detalle de un carrito de compra a partir de los datos de la base de datos
     * @param row Datos en el formato de la base de datos
     * @return Datos en el formato de las clases de java
     */
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

    /**
     * Convierte los datos de detalle al formato de la base de datos
     * @param detalle Datos de los detalles
     * @return datos en formato de la base de datos
     */
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

    public int contarDetallesExistintes() throws IOException {
        String rango = CarritoConstantes.CANT_DETALLES;// Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    public void guardarDetalle(DetalleCarrito detalle) throws IOException {

        int detalles=contarDetallesExistintes();
        String range = SHEET_NAME_DETALLE+"!A"+(2+detalles)+":"+ CarritoConstantes.COL_REGISTRO_DETALLE_FINAL+(2+detalles);

        List<List<Object>> values = Arrays.asList(
                mapearDetalleCarritoInverso(detalle)
        );

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Numero de celdas actualizadas: " + result.getUpdatedCells());
    }

    public int obtenerIndiceDetalle(String idCarrito,String productoId) {
        List<DetalleCarrito> detalles = null;
        int filaCuenta=-1;
        try {
            detalles = obtenerDetallesCarrito();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        int tam=detalles.size();
        for (int i=0;i<tam;i++) {
            if (detalles.get(i).getProductoId().equals(productoId) &&
                    detalles.get(i).getIdCarrito().equals(idCarrito)) {
                filaCuenta=i;
                break;
            }
        }
        return filaCuenta;
    }

    public void actualizarDetalle(DetalleCarrito detalle) throws IOException {
        int indice=obtenerIndiceDetalle(detalle.getIdCarrito(),detalle.getProductoId());
        if (indice!=-1) {
            String range = SHEET_NAME_DETALLE+"!A"+(2+indice)+":"+ CarritoConstantes.COL_REGISTRO_DETALLE_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearDetalleCarritoInverso(detalle)
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

    public void eliminarDetalle(DetalleCarrito detalle) throws IOException {
        int indice=obtenerIndiceDetalle(detalle.getIdCarrito(),detalle.getProductoId());
        if (indice!=-1) {
            String range = SHEET_NAME_DETALLE+"!A"+(2+indice)+":"+ CarritoConstantes.COL_REGISTRO_DETALLE_FINAL+(2+indice);
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

    public Optional<CarritoCompras> obtenerPorIdUsuario(String idUsuario) throws IOException {
        List<CarritoCompras> carritosObtenidos=
                filtrar(carrito -> carrito.getIdUsuario().equals(idUsuario));
        if (carritosObtenidos.isEmpty()) {
            return Optional.empty();
        }
        if (carritosObtenidos.size()>1) {
            throw new IOException("Mas de un carrite tiene ese usuario");
        }
        return Optional.of(carritosObtenidos.get(0));
    }

    public void eliminarDetalles(List<DetalleCarrito> detallesEliminar) throws IOException {
        for (DetalleCarrito detalle : detallesEliminar) {
            eliminarDetalle(detalle);
        }
    }
}
