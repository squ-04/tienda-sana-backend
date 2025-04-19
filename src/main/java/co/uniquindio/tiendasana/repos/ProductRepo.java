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
    private final String SHEET_GENERAL= ProductoConstantes.HOJAADMIN;

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

    /**
     * Método obtener las filas de una hoja como lista de productos
     * @param hoja, hoja a la cual se le desean extraer las filas
     * @return Lista de productos
     * @throws IOException
     * @throws ProductoParseException
     */
    public List<Producto> obtenerProductos(String hoja) throws IOException, ProductoParseException {
        List<List<Object>> filas = obtenerFilasHoja(hoja);
        return mapearFilasProductos(filas);
    }

    /**
     * Este metodo sirve para obtener todas las filas de una hoja dada, esto se representa como una lista de listas
     * de objetos, debido a que cada una de las filas se representa como una lista de onjetos
     * @param hoja, hoja a la cual se le extraen las filas ne formato Java
     * @return
     * @throws IOException
     */
    private List<List<Object>> obtenerFilasHoja(String hoja) throws IOException {
        String rango = hoja + "!A2:"+ ProductoConstantes.COL_REGISTRO_FINAL; // ID, Nombre, Estado, Localidad, PrecioReserva
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores=respuesta.getValues();
        if (valores!=null) {
            return valores;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Método que cuenta trae la celda que cuenta la cantidad de productos en la hoja destinada a los productos que se
     * le van a mostrar al cliente
     * @return int que indidca la cantidad de productos que hay en la hoja de productos del cliente
     * @throws IOException
     */
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
                e.printStackTrace();
                throw new ProductoParseException("Error en el parseo de cantidad o precio unitario del producto en fila "+ row);
            }
        }
        return productos;
    }

    /**
     * Métddo con el cual una lista de objetos se transforma en un producto (Entidad)
     * @param row Lista de objetos a transformar (Castear) en producto
     * @return
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

    /**
     * Método para mapear un producto como Lista de objetos, ya que es este tipo de dato el que la integracion
     * con Google Sheets permite para hacer la escritura sobre la hoja de calculo
     * @param producto el cual será el producto para tranforma en lista de objetos
     * @return Lista de objetos que representa el producto
     */
    //TODO verificar si es valido para el estado
    public List<Object> mapearProductoInverso(Producto producto) {
        return Arrays.asList(
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria(),
                producto.getEstado(),
                producto.getCantidad(),
                producto.getImagen(),
                ((int)producto.getPrecioUnitario()),
                producto.getId()
        );
    }

    /**
     * Método para fitrar dada una expresión de tipo Predictate y una hoja en la cual se debe de filtrar
     * @param expresion
     * @param hoja
     * @return
     * @throws IOException
     * @throws ProductoParseException
     */
    public List<Producto> filtrar (Predicate<Producto> expresion, String hoja) throws IOException, ProductoParseException {
        List<Producto> productos = obtenerProductos(hoja);
        return productos.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    /**
     * Método para obtener el indice de un producto en una hoja que tiene como parametro
     * @param id
     * @param hoja, hoja donde se buscara el producto
     * @return int que indica la posicion del producto en las filas validas de la hoja de cáclculo
     */
    public int obtenerIndiceProducto(String id, String hoja) {
        List<Producto> productos = null;
        int filaCuenta=-1;
        try {
            productos = obtenerProductos(hoja);
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



    /**
     * Metodo que busca actualizar una de las filas en la hoja de cálculo de productos que se comparte con Softr,
     * actualiza toda la fila dada la nueva información de la entidad producto
     * @param producto
     * @throws IOException
     */
    public void actualizar(Producto producto) throws IOException {
        int indice= obtenerIndiceProducto(producto.getId(),SHEET_GENERAL);
        if (indice!=-1) {
            String range = SHEET_GENERAL +"!A"+(2+indice)+":"+ ProductoConstantes.COL_REGISTRO_FINAL+(2+indice);
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

    /**
     * Método para obtener un producto como entidad, dado un id, buscandolo en la base de datos de
     * Google sheets
     * @param id
     * @return Optional del producto
     * @throws IOException
     * @throws ProductoParseException
     */
    public Optional<Producto> obtenerPorId(String id) throws IOException, ProductoParseException {
        List<Producto> productosObtenidos=
                filtrar(producto -> producto.getId().equals(id), SHEET_NAMECLIENTE);
        if (productosObtenidos.isEmpty()) {
            return Optional.empty();
        }
        if (productosObtenidos.size()>1) {
            throw new IOException("Mas de un producto tiene ese id");
        }
        return Optional.of(productosObtenidos.get(0));
    }

}
