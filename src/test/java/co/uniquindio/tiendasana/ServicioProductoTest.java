package co.uniquindio.tiendasana;

import co.uniquindio.tiendasana.services.implementations.ProductServiceImp;
import co.uniquindio.tiendasana.services.interfaces.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ServicioProductoTest {

    @Autowired
    private ProductServiceImp productService;

    @Test
    public void insertarTest(){
        try {
            productService.insertDataIntoSheet("Hola", "Pacho", "test");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void leerTest(){
        try {
            productService.leerDatos();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
