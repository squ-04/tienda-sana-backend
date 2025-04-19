package co.uniquindio.tiendasana;

import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.documents.VentaProducto;
import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.*;
import co.uniquindio.tiendasana.repos.CarritoComprasRepo;
import co.uniquindio.tiendasana.repos.CuentaRepo;
import co.uniquindio.tiendasana.repos.ProductRepo;
import co.uniquindio.tiendasana.repos.VentaProductoRepo;
import co.uniquindio.tiendasana.services.implementations.ProductoServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ServicioProductoTest {

    @Autowired
    private ProductoServiceImp productService;
    @Autowired
    private CuentaRepo cuentaRepo;
    @Autowired
    private ProductRepo productoRepo;
    @Autowired
    private CarritoComprasRepo carritoComprasRepo;
    @Autowired
    private VentaProductoRepo ventaProductoRepo;
    /**
    @Autowired
    private AssertTrueValidator assertTrueValidator;
     */

    public Cuenta quemarCuenta() {
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

    public Producto quemarProducto() {
        return Producto.builder()
                .nombre("Ensalada Mediterranea")
                .descripcion("Ensalada fresca con lechuga, tomate cherry, pepino, aceitunas negras, queso feta y aderezo de aceite de oliva.")
                .categoria("Comida Saludable")
                .estado("Disponible")
                .cantidad(30)
                .imagen("https://ejemplo.com/imagenes/ensalada-mediterranea.jpg")
                .precioUnitario(24900)
                .id("d0d73458-cc6f-44dd-821a-07b35329ca2b")
                .build();
    }

    public DetalleCarrito quemarDetalleCarrito(String idCarrito,String productoId) {
        return DetalleCarrito.builder()
                .productoId(productoId)
                .cantidad(10)
                .subtotal(24900*10)
                .idCarrito(idCarrito)
                .build();
    }

    public CarritoCompras quemarCarritoCompras(String idUsuario) {
        return CarritoCompras.builder()
                .id("2014b7e7-182a-45fe-8275-80e415a5d4e6")
                .fecha(LocalDateTime.of(2025, 4, 18, 12, 30))
                .idUsuario(idUsuario)
                .productos(List.of(
                        quemarDetalleCarrito("2014b7e7-182a-45fe-8275-80e415a5d4e6",
                        "DB7b4VWUDxSm9jkRxACfU7")))
                .build();
    }

    public DetalleVentaProducto quemarDetalleVentaProducto(String idVenta,String productoId) {
        return DetalleVentaProducto.builder()
                .productoId(productoId)
                .cantidad(10)
                .valor(24900*10)
                .ventaId(idVenta)
                .build();
    }

    public VentaProducto quemarVentaProducto(String idUsuario,String productoId) {
        Pago pago = Pago.builder()
                .id("1334999969")
                .currency("COP")
                .paymentType("credit_card")
                .statusDetail("accredited")
                .authorizationCode("301299")
                .date(LocalDateTime.of(2025, 4, 18, 1, 45))
                .transactionValue(20000)
                .status("approved")
                .build();

        return VentaProducto.builder()
                .id("4e20ab9f-3b3b-4bee-95a1-93a64aed9af7")
                .emailUsario(idUsuario)
                .fecha(LocalDateTime.of(2025, 4, 18, 1, 30))
                .total(20000)
                .promocionId("-")
                .codigoPasarela("1190282227-39b67251-5fb1-4237-8d8b-e5ce8d178c6e")
                .productos(List.of(
                        quemarDetalleVentaProducto("4e20ab9f-3b3b-4bee-95a1-93a64aed9af7",
                                productoId)
                ))
                .pago(pago)
                .build();
    }

    @Test
    public void obtenerCuentas() {

    }

    @Test
    public void obtenerProductos() {

    }

    @Test
    public void obtenerDetallesCarritoCompras() {

    }

    @Test
    public void obtenerCarritoCompras() {

    }

    @Test
    public void obtenerDetallesVentaProducto() {

    }

    @Test
    public void obtenerVentaProducto() {

    }

    @Test
    public void mapearCuentas() {

    }

    @Test
    public void mapearProductos() {

    }

    @Test
    public void mapearDetallesCarritoCompras() {

    }

    @Test
    public void mapearCarritoCompras() {

    }

    @Test
    public void mapearDetallesVentaProducto() {

    }

    @Test
    public void mapearVentaProducto() {

    }

}
