package co.uniquindio.tiendasana;

import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.CodigoValidacion;
import co.uniquindio.tiendasana.model.vo.Usuario;
import co.uniquindio.tiendasana.repos.CuentaRepo;
import co.uniquindio.tiendasana.repos.ProductRepo;
import co.uniquindio.tiendasana.services.implementations.ProductoServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ServicioProductoTest {

    @Autowired
    private ProductoServiceImp productService;
    @Autowired//TODO eliminar este repositorio(su intancia, no la clase) una vez ya no se necesite
    private CuentaRepo cuentaRepo;
    @Autowired
    private ProductRepo productoRepo;
    /**
    @Autowired
    private AssertTrueValidator assertTrueValidator;
     */

    @Test
    public void insertarTest(){
        try {
            productService.insertDataIntoSheet("Hola", "buenasssss", "test");
            assertTrue(true);
        }catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public Cuenta quemarCliente() {
        Usuario usuario = Usuario.builder()
                .dni("1090")
                .nombre("Juan")
                .telefono("314")
                .direccion("Mi casa")
                .build();

        CodigoValidacion codigoValidacionRegistro= CodigoValidacion.builder()
                .codigo("abcd")
                .fechaCreacion(LocalDateTime.now().plusDays(1))
                .build();

        CodigoValidacion codigoValidacionContrasenia= CodigoValidacion.builder()
                .codigo("efgh")
                .fechaCreacion(LocalDateTime.now().plusDays(2))
                .build();

        return Cuenta.builder()
                .usuario(usuario)
                .email("corre@gmail.com")
                .contrasenia("1234")
                .rol(Rol.CLIENTE)
                .estado(EstadoCuenta.ACTIVA)
                .fechaRegistro(LocalDateTime.now())
                .codigoValidacionContrasenia(codigoValidacionContrasenia)
                .codigoValidacionRegistro(codigoValidacionRegistro)
                .build();
    }

    @Test
    public void ingresarClienteTest(){
        Cuenta cuenta= quemarCliente();
        try {
            cuentaRepo.guardar(cuenta);
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void actualizarClienteTest(){
        Cuenta cuenta= quemarCliente();
        cuenta.setContrasenia("Mega segura");
        cuenta.setRol(Rol.ADMIN);
        cuenta.setEmail("c@gmail.com");
        try {
            cuentaRepo.actualizar(cuenta);
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void obtenerProductos() {
        try {
            productoRepo.obtenerProductos();
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}
