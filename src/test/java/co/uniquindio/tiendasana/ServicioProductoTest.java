package co.uniquindio.tiendasana;

import co.uniquindio.tiendasana.services.implementations.ProductoServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ServicioProductoTest {

    @Autowired
    private ProductoServiceImp productService;

    @Test
    public void insertarTest(){
        try {
            productService.insertDataIntoSheet("Hola", "buenasssss", "test");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
