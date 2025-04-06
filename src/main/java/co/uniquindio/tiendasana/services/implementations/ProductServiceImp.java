package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.productDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoInfoDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoItemDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.repos.ProductRepo;
import co.uniquindio.tiendasana.services.interfaces.ProductService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImp implements ProductService {
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;
    private final ProductRepo productRepo;

    public ProductServiceImp(ProductRepo productRepo, Sheets sheetsService) {
        this.productRepo = productRepo;
        this.sheetsService = sheetsService;
    }


    // Nuevo método para insertar valores en la hoja de cálculo
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
     *
     * @param id
     * @return
     */
    @Override
    public ProductoInfoDTO obtenerInfoProducto(String id) {
        return null;
    }

    @Override
    public List<ProductoItemDTO> obtenerProductosCliente() throws IOException, ProductoParseException {
        List<Producto> productos = productRepo.ObtenerProductos();

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

        return productosItems;
    }
}
