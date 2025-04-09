package co.uniquindio.tiendasana.repos;


import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.Producto;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para la interacción con la hoja de calculo correspondiente a los
 * productos
 */
@Repository
public class ProductRepo  {

    private final Sheets sheetsService;
    @Value("${google.sheets.spreadsheet-id}")
    private  String spreadsheetId;

    private final String SHEET_NAME= "Producto";

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
    public List<Producto> ObtenerProductos() throws IOException, ProductoParseException {
        List<List<Object>> filas = obtenerFilasHoja();
        return mapearFilasProductos(filas);
    }

    /**
     * Metodo usado para obtener las filas de la hoja como Objetos de Java
     * @return Lista de listas de objetos
     * @throws IOException
     */
    private List<List<Object>> obtenerFilasHoja() throws IOException {
        String rango = SHEET_NAME +"!A2:H";
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

}
