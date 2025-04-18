package co.uniquindio.tiendasana.repos;


import co.uniquindio.tiendasana.dto.productodtos.ProductosTotal;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.utils.ProductoConstantes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Clase para la interacción con la hoja de calculo correspondiente a los
 * productos
 */
@Repository
public class ProductRepo  {

    private final Sheets sheetsService;
    @Value("${google.sheets.spreadsheet-id}")
    private  String spreadsheetId;

    private final String SHEET_NAMECLIENTE = ProductoConstantes.HOJACLIENTE;

    /**
     * Metodo contructor de la clase
     * @param sheetsService
     */
    public ProductRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    /**
     *  Metodo publico usado para obtener las fils de la hoja Producto como intancias de la clase
     *  Producto
     * @return Lista de productos en la hoja
     * @throws IOException
     * @throws ProductoParseException
     */
    public ProductosTotal obtenerProductos(int pagina, int cantidadElementos) throws IOException, ProductoParseException {
        int totalProductos= contarProductosExistintes();
        List<List<Object>> filas = obtenerFilasHoja(pagina,cantidadElementos,totalProductos);
        List<Producto> productos = mapearFilasProductos(filas);
       return new ProductosTotal( totalProductos, productos);
    }

    public List<Producto> obtenerProductos() throws IOException, ProductoParseException {
        List<List<Object>> filas = obtenerFilasHoja();
        return mapearFilasProductos(filas);
    }

    private List<List<Object>> obtenerFilasHoja() throws IOException {
        String rango = SHEET_NAMECLIENTE + "!A2:"+ ProductoConstantes.COL_REGISTRO_FINAL; // ID, Nombre, Estado, Localidad, PrecioReserva
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
    }

    public int contarProductosExistintes() throws IOException {
        String rango = SHEET_NAMECLIENTE + ProductoConstantes.CANT_PRODUCTOS; // Ajusta según columnas
        List<List<Object>> respuesta =
                sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute().getValues();
        return Integer.parseInt(respuesta.get(0).get(0).toString());
    }

    /**
     * Metodo usado para obtener las filas de la hoja como Objetos de Java
     * @return Lista de listas de objetos
     * @throws IOException
     */
    private List<List<Object>> obtenerFilasHoja(int pagina, int cantidad, int cantidadTotal) throws IOException {
        
        int cantidadPaginas=cantidadTotal/cantidad;

        if(pagina > cantidadPaginas){
            throw new RuntimeException("La página no existe");//TODO cambiar excepcion por Exception
        }
        int filaInicio = 2 + (pagina * cantidad); // A2 es la primera fila de datos
        int filaFin = filaInicio + cantidad - 1;

        String rango = SHEET_NAMECLIENTE + "!A" + filaInicio + ":H" + filaFin;
        ValueRange respuesta= sheetsService.spreadsheets().values().get(spreadsheetId,rango).execute();

        return respuesta.getValues();
    }

    /**
     * Metodo usado para hacer un casteo o mapero de los objetos retornados por la
     * hoja a instacias de la clase Producto
     * @param filas
     * @return
     * @throws ProductoParseException
     */
    private List<Producto> mapearFilasProductos(List<List<Object>> filas) throws ProductoParseException {
        List<Producto> productos = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                Producto producto=mapearProducto(row);
                productos.add(producto);
            }catch (NumberFormatException e){
                throw new ProductoParseException("Error en el parseo de cantidad o precio unitario del producto en fila "+ row);
            }
        }
        return productos;
    }
    /**
    private List<Producto> mapearFilasProductos(List<List<Object>> filas) throws ProductoParseException {
        List<Producto> productos = new ArrayList<>();
        for (List<Object> row : filas) {
            try {
                Producto producto = Producto.builder()
                        .nombre(!row.isEmpty() ? row.get(0).toString() : null)
                        .descripcion(row.size() > 1 ? row.get(1).toString() : null)
                        .categoria(row.size() > 2 ? row.get(2).toString() : null)
                        .estado(row.size() > 3 ? row.get(3).toString() : null)
                        .cantidad(row.size() > 4 ? Integer.parseInt(row.get(4).toString()) : -1)
                        .imagen(row.size() > 5 ? row.get(5).toString() : null)
                        .precioUnitario(row.size() > 6 ? Float.parseFloat(row.get(6).toString()) : -1.0f)
                        .id(row.size() > 7 ? row.get(7).toString() : null)
                        .build();
                productos.add(producto);
            }catch (NumberFormatException e){
                throw new ProductoParseException("Error en el parseo de cantidad o precio unitario del producto en fila "+ row);
            }
        }
        return productos;
    }
    */

    public Producto mapearProducto(List<Object> row) {
        String nombre = row.get(0).toString();
        String descripcion= row.get(1).toString();
        String categoria = row.get(2).toString();
        String estado = row.get(3).toString();
        int cantidad=Integer.parseInt(row.get(4).toString());
        String imagen=row.get(5).toString();
        float precioUnitario=Float.parseFloat(row.get(6).toString());
        String id=row.get(7).toString();

        return Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .categoria(categoria)
                .estado(estado)
                .cantidad(cantidad)
                .imagen(imagen)
                .precioUnitario(precioUnitario)
                .id(id)
                .build();
    }

    //TODO verificar si es valido para el estado
    public List<Object> mapearProductoInverso(Producto producto) {
        return Arrays.asList(
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria(),
                producto.getEstado(),
                ""+producto.getCantidad(),
                producto.getImagen(),
                ""+((int)producto.getPrecioUnitario()),
                producto.getId()
        );
    }

    public List<Producto> filtrar (Predicate<Producto> expresion) throws IOException, ProductoParseException {
        List<Producto> productos = obtenerProductos();
        return productos.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    public int obtenerIndiceProducto(String id) {
        List<Producto> productos = null;
        int filaCuenta=-1;
        try {
            productos = obtenerProductos();
        } catch (IOException | ProductoParseException e) {
            throw new RuntimeException();
        }
        int tam=productos.size();
        for (int i=0;i<tam;i++) {
            if (productos.get(i).getId().equals(id)) {
                filaCuenta=i;
                break;
            }
        }
        return filaCuenta;
    }

    public void actualizar(Producto producto) throws IOException {
        int indice=obtenerIndiceProducto(producto.getId());
        if (indice!=-1) {
            String range = SHEET_NAMECLIENTE +"!A"+(2+indice)+":"+ ProductoConstantes.COL_REGISTRO_FINAL+(2+indice);
            List<List<Object>> values = Arrays.asList(
                    mapearProductoInverso(producto)
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

    public Optional<Producto> obtenerPorId(String id) throws IOException, ProductoParseException {
        List<Producto> productosObtenidos=
                filtrar(producto -> producto.getId().equals(id));
        if (productosObtenidos.isEmpty()) {
            return Optional.empty();
        }
        if (productosObtenidos.size()>1) {
            throw new IOException("Mas de un producto tiene ese id");
        }
        return Optional.of(productosObtenidos.get(0));
    }

}
