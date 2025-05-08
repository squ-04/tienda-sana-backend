package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.productodtos.*;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.repos.ProductRepo;
import co.uniquindio.tiendasana.services.interfaces.ProductoService;
import co.uniquindio.tiendasana.utils.ProductoConstantes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImp implements ProductoService {
    private final Sheets sheetsService;
    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;
    private final ProductRepo productRepo;

    /**
     * Metodo contructor de la clase
     * @param productRepo
     * @param sheetsService
     */
    public ProductoServiceImp(ProductRepo productRepo, Sheets sheetsService) {
        this.productRepo = productRepo;
        this.sheetsService = sheetsService;
    }


    /**
     * Inserta una fila de datos en la hoja 'Test' (columnas A, B y C).
     * @param value1 Valor que irá en la columna A.
     * @param value2 Valor que irá en la columna B.
     * @param value3 Valor que irá en la columna C.
     * @throws IOException Si falla la llamada a la API de Sheets.
     */
    public void insertDataIntoSheet(String value1, String value2, String value3) throws IOException {
        // Definir el rango de celdas a insertar (Hoja 'Test', columnas A, B, y C)
        String range = "Test!A4:C4"; // Esto corresponde a las columnas A a C de la hoja 'Test'

        // Crear el valor que vamos a insertar
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(value1, value2, value3)
        );

        // Crear un objeto ValueRange que contiene los valores a insertar
        ValueRange body = new ValueRange().setValues(values);

        // Llamar a la API para insertar los valores
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" para insertar como está
                .execute();

        System.out.println("Número de celdas actualizadas: " + result.getUpdatedCells());
    }

    /**
     * Metodo usado para obtener los detalles de un producto
     * @param id ID del producto.
     * @return DTO con toda la información del producto.
     * @throws Exception Si el producto no existe o hay error al acceder en la base de datos
     */
    @Override
    public ProductoInfoDTO obtenerInfoProducto(String id) throws Exception {
        try {
            Optional<Producto> productoObtenido= productRepo.obtenerPorId(id);
            if (productoObtenido.isEmpty()) {
                throw new Exception("Producto no encontrado");
            }
            Producto producto=productoObtenido.get();
            return new ProductoInfoDTO(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getCategoria(),
                    producto.getDescripcion(),
                    producto.getImagen(),
                    producto.getPrecioUnitario(),
                    producto.getCantidad()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ProductoParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo usado para obtener lo informacion de los productos que los clientes
     * verán en primera instancia
     * @param pagina Número de pagina (empezando en 0).
     * @return Objeto con total de páginas y lista de items de productos.
     * @throws IOException Si al acceder a la base de datos
     * @throws ProductoParseException  Si al parsear un producto ocurre un error.
     */
    @Override
    public ListaProductos obtenerProductosCliente(int pagina) throws IOException, ProductoParseException {
        ProductosTotal paginaProductos = productRepo.obtenerProductos(pagina, ProductoConstantes.ELEMENTOSPAGINA);
        List<Producto> productos=paginaProductos.productos();
        List<ProductoItemDTO> productosItems = productos.stream()
                .filter(producto -> "Disponible".equalsIgnoreCase(producto.getEstado()))
                .filter(producto -> producto.getCantidad() > 0)
                .map(producto -> new ProductoItemDTO(
                        producto.getId(),
                        producto.getNombre(),
                        producto.getCategoria(),
                        producto.getImagen(),
                        producto.getPrecioUnitario()
                ))
                .collect(Collectors.toList());

        return new ListaProductos(
                (int) Math.ceil((double) paginaProductos.totalProductos() / ProductoConstantes.ELEMENTOSPAGINA),
                productosItems
        );
    }

    /**
     * Obtener un producto (Entidad) dado su ID
     * @param id Id del producto
     * @return Producto obtenido
     * @throws ProductoParseException Si el producto no existe
     * @throws IOException Error al acceder a la base de datos
     */
    @Override
    public Producto getProducto(String id) throws ProductoParseException, IOException {
        Optional<Producto> productoOptional= productRepo.obtenerPorId(id);
        if (productoOptional.isEmpty()) {
            throw new ProductoParseException("Producto no encontrado");
        }
        Producto producto=productoOptional.get();
        return producto;
    }

    /**
     * Metodo para reducir la cantidad de Stock de un producto una vez se tenga un pago aprovado y acreditado
     * @param id Id del producto
     * @param cantidadComprada Cantidad que del producto que se compro y que se va a deducir
     * @throws Exception Error al acceder a la base de datos
     * o la cantidad a comprar es mal alta que el stock
     */
    @Override
    public void reducirCantidadProductosStock(String id, int cantidadComprada) throws Exception {
        Producto producto= getProducto(id);
        if (producto.getCantidad()-cantidadComprada < 0) {
            throw new Exception ("La compra del producto "+producto.getNombre() +" es alta para el stock");
        }
        producto.setCantidad(producto.getCantidad()-cantidadComprada);
        productRepo.actualizar(producto);
        System.out.println("Se ha actualizado el producto correctamente");
    }

    @Override
    public List<ProductoItemDTO> filtrarProductos(FiltroProductoDTO filtroProductoDTO) throws Exception {
        return List.of();
    }


}
