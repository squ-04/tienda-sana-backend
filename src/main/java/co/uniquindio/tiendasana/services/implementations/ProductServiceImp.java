package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.productDTO;
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

@Service
public class ProductServiceImp implements ProductService {
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    public ProductServiceImp( Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }


    //Gson

    public void leerDatos () throws IOException {


        String rango = "Producto" + "!B2:I2"; // Rango para obtener la fila 2

        ValueRange respuesta = sheetsService.spreadsheets().values()
                .get(spreadsheetId, rango)
                .execute();

        List<List<Object>> valores = respuesta.getValues();

        if (valores == null || valores.isEmpty()) {
            System.out.println("No se encontraron datos.");
        }
        System.out.println(valores);

        Producto producto = Producto
                .builder()
                .nombre(valores.get(0).get(0).toString())
                .descripcion(valores.get(0).get(1).toString())
                .categoria(valores.get(0).get(2).toString())
                .estado(valores.get(0).get(3).toString())
                .cantidad(Integer.parseInt(valores.get(0).get(4).toString()))
                .build();

        System.out.println(producto);
    }

    // Nuevo método para insertar valores en la hoja de cálculo
     public void insertDataIntoSheet(String value1, String value2, String value3) throws IOException {
        // Definir el rango de celdas a insertar (Hoja 'Test', columnas A, B, y C)
        String range = "Test!A2:C2"; // Esto corresponde a las columnas A a C de la hoja 'Test'

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


}
