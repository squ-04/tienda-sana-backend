package co.uniquindio.tiendasana;

import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.documents.VentaProducto;
import co.uniquindio.tiendasana.model.enums.CategoriaProducto;
import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.*;
import co.uniquindio.tiendasana.repos.CarritoComprasRepo;
import co.uniquindio.tiendasana.repos.CuentaRepo;
import co.uniquindio.tiendasana.repos.ProductRepo;
import co.uniquindio.tiendasana.repos.VentaProductoRepo;
import co.uniquindio.tiendasana.services.implementations.ProductoServiceImp;
import co.uniquindio.tiendasana.utils.ProductoConstantes;
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

    /**
     * Metodo que crea una cuenta de usuario para pruebas
     * @return Cuenta
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

    /**
     * Metodo que crea un producto para pruebas
     * @return Producto
     */
    public Producto quemarProducto() {
        return Producto.builder()
                .nombre("Ensalada Mediterranea")
                .descripcion("Ensalada fresca con lechuga, tomate cherry, pepino, aceitunas negras, queso feta y aderezo de aceite de oliva.")
                .categoria("Frutos secos")
                .estado("Disponible")
                .cantidad(30)
                .imagen("https://ejemplo.com/imagenes/ensalada-mediterranea.jpg")
                .precioUnitario(24900)
                .id("d0d73458-cc6f-44dd-821a-07b35329ca2b")
                .build();
    }

    /**
     * Metodo que crea un detalle de carrito de compras para pruebas
     * @return DetalleCarrito
     */
    public DetalleCarrito quemarDetalleCarrito(String idCarrito,String productoId) {
        return DetalleCarrito.builder()
                .productoId(productoId)
                .cantidad(10)
                .subtotal(24900*10)
                .idCarrito(idCarrito)
                .build();
    }

    /**
     * Metodo que crea un carrito de compras para pruebas
     * @return CarritoCompras
     */
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

    /**
     * Metodo que crea un detalle de venta de producto para pruebas
     * @return DetalleVentaProducto
     */
    public DetalleVentaProducto quemarDetalleVentaProducto(String idVenta,String productoId) {
        return DetalleVentaProducto.builder()
                .productoId(productoId)
                .cantidad(10)
                .valor(24900*10)
                .ventaId(idVenta)
                .build();
    }

    /**
     * Metodo que crea una venta de producto para pruebas
     * @return VentaProducto
     */
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

    /**
     * Metodo para probar la obtencion de cuentas
     */
    @Test
    public void obtenerCuentas() {
        try {
            List<Cuenta> cuentas=cuentaRepo.obtenerCuentas();
            assertNotNull(cuentas);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Metodo para probar la obtencion de productos
     */
    @Test
    public void obtenerProductos() {
        try {
            List<Producto> productos=productoRepo.obtenerProductos(ProductoConstantes.HOJAADMIN);
            assertNotNull(productos);
        } catch (IOException | ProductoParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Metodo para probar la obtencion de productos que se le muestran al cliente
     */
    @Test
    public void obtenerProductosCliente() {
        try {
            List<Producto> productos=productoRepo.obtenerProductos(ProductoConstantes.HOJACLIENTE);
            assertNotNull(productos);
        } catch (IOException | ProductoParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Metodo para probar la obtencion de detalles de carrito de compras
     */
    @Test
    public void obtenerDetallesCarritoCompras() {
        try {
            List<DetalleCarrito> detalleCarritos=carritoComprasRepo.obtenerDetallesCarrito();
            assertNotNull(detalleCarritos);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Metodo para probar la obtencion de carritos de compras sin sus detalles
     */
    @Test
    public void obtenerCarritosComprasSimples() {
        try {
            List<CarritoCompras> carritoCompras=carritoComprasRepo.obtenerCarritosSimples();
            if (carritoCompras==null) {
                fail();
            }
            for (CarritoCompras carrito: carritoCompras) {
                if (carrito.getProductos() != null) {
                    fail();
                }
            }
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Metodo para probar la obtencion de detalles de venta de productos
     */
    @Test
    public void obtenerDetallesVentaProducto() {
        try {
            List<DetalleVentaProducto> detallesVenta=ventaProductoRepo.obtenerDetallesVenta();
            assertNotNull(detallesVenta);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Metodo para probar la obtencion de ventas de productos sin sus detalles
     */
    @Test
    public void obtenerVentaProducto() {
        try {
            List<VentaProducto> ventasProductos=ventaProductoRepo.obtenerVentasSimples();
            if (ventasProductos==null) {
                fail();
            }
            for (VentaProducto venta: ventasProductos) {
                if (venta.getProductos() != null) {
                    fail();
                }
            }
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Metodo para probar el mapeo de cuentas
     */
    @Test
    public void mapearCuentas() {
        Cuenta cuenta=quemarCuenta();
        List<Object> datosMapeados=cuentaRepo.mapearCuentaInverso(cuenta);
        if (datosMapeados==null || datosMapeados.size()!=13) {
            fail();
        }
        Cuenta cuentaMapeada=cuentaRepo.mapearCuenta(datosMapeados);
        if (cuentaMapeada==null) {
            fail();
        }
        assertAll("Verificar que los datos se mantengan igual despues de los mapeos",
                () -> assertEquals(cuenta.getUsuario().getDni(),
                        cuentaMapeada.getUsuario().getDni()),
                () -> assertEquals(cuenta.getUsuario().getNombre(),
                        cuentaMapeada.getUsuario().getNombre()),
                () -> assertEquals(cuenta.getUsuario().getTelefono(),
                        cuentaMapeada.getUsuario().getTelefono()),
                () -> assertEquals(cuenta.getUsuario().getDireccion(),
                        cuentaMapeada.getUsuario().getDireccion()),
                () -> assertEquals(cuenta.getEmail(),cuentaMapeada.getEmail()),
                () -> assertEquals(cuenta.getContrasenia(),cuentaMapeada.getContrasenia()),
                () -> assertEquals(cuenta.getRol(),cuentaMapeada.getRol()),
                () -> assertEquals(cuenta.getEstado(),cuentaMapeada.getEstado()),
                () -> assertEquals(cuenta.getFechaRegistro(),cuentaMapeada.getFechaRegistro()),
                () -> assertEquals(cuenta.getCodigoValidacionRegistro().getCodigo(),
                        cuentaMapeada.getCodigoValidacionRegistro().getCodigo()),
                () -> assertEquals(cuenta.getCodigoValidacionRegistro().getFechaCreacion(),
                        cuentaMapeada.getCodigoValidacionRegistro().getFechaCreacion()),
                () -> assertEquals(cuenta.getCodigoValidacionContrasenia().getCodigo(),
                        cuentaMapeada.getCodigoValidacionContrasenia().getCodigo()),
                () -> assertEquals(cuenta.getCodigoValidacionContrasenia().getFechaCreacion(),
                        cuentaMapeada.getCodigoValidacionContrasenia().getFechaCreacion())
        );

    }

    /**
     * Metodo para probar el mapeo de productos desde la base de datos
     */
    @Test
    public void mapearProductos() {
        Producto producto=quemarProducto();
        List<Object> datosMapeados=productoRepo.mapearProductoInverso(producto);
        System.out.println(datosMapeados);
        if (datosMapeados==null || datosMapeados.size()!=8) {
            fail();
        }
        Producto productoMapeado=productoRepo.mapearProducto(datosMapeados);
        if (productoMapeado==null) {
            fail();
        }
        assertAll("Verificar que los datos se mantengan igual despues de los mapeos",
                () -> assertEquals(producto.getNombre(),productoMapeado.getNombre()),
                () -> assertEquals(producto.getDescripcion(),productoMapeado.getDescripcion()),
                () -> assertEquals(producto.getCategoria(),productoMapeado.getCategoria()),
                () -> assertEquals(producto.getEstado(),productoMapeado.getEstado()),
                () -> assertEquals(producto.getCantidad(),productoMapeado.getCantidad()),
                () -> assertEquals(producto.getImagen(),productoMapeado.getImagen()),
                () -> assertEquals(producto.getPrecioUnitario(),productoMapeado.getPrecioUnitario()),
                () -> assertEquals(producto.getId(),productoMapeado.getId())
        );
    }

    /**
     * Metodo para probar el mapeo de los detalles de carritos de compras desde la base de datos
     */
    @Test
    public void mapearDetallesCarritoCompras() {
        Cuenta cuenta=quemarCuenta();
        Producto producto=quemarProducto();
        DetalleCarrito detalleCarrito=quemarDetalleCarrito(cuenta.getEmail(),producto.getId());
        List<Object> datosMapeados=carritoComprasRepo.mapearDetalleCarritoInverso(detalleCarrito);
        if (datosMapeados==null || datosMapeados.size()!=4) {
            fail();
        }
        DetalleCarrito detalleCarritoMapeado=carritoComprasRepo.mapearDetalleCarrito(datosMapeados);
        if (detalleCarritoMapeado==null) {
            fail();
        }
        assertAll("Verificar que los datos se mantengan igual despues de los mapeos",
                () -> assertEquals(detalleCarrito.getProductoId(),detalleCarritoMapeado.getProductoId()),
                () -> assertEquals(detalleCarrito.getCantidad(),detalleCarritoMapeado.getCantidad()),
                () -> assertEquals(detalleCarrito.getSubtotal(),detalleCarritoMapeado.getSubtotal()),
                () -> assertEquals(detalleCarrito.getIdCarrito(),detalleCarritoMapeado.getIdCarrito())
        );
    }

    /**
     * Metodo para probar el mapeo de los carritos de compras desde la base de datos
     */
    @Test
    public void mapearCarritoCompras() {
        Cuenta cuenta=quemarCuenta();
        CarritoCompras carrito=quemarCarritoCompras(cuenta.getEmail());
        List<Object> datosMapeados=carritoComprasRepo.mapearCarritoInverso(carrito);
        if (datosMapeados==null || datosMapeados.size()!=3) {
            fail();
        }
        CarritoCompras carritoMapeado=carritoComprasRepo.mapearCarrito(datosMapeados);
        if (carritoMapeado==null) {
            fail();
        }
        assertAll("Verificar que los datos se mantengan igual despues de los mapeos",
                () -> assertEquals(carrito.getId(),carritoMapeado.getId()),
                () -> assertEquals(carrito.getFecha(),carritoMapeado.getFecha()),
                () -> assertEquals(carrito.getIdUsuario(),carritoMapeado.getIdUsuario())
        );
    }

    /**
     * Metodo para probar el mapeo de los detalles de venta de productos desde la base de datos
     */
    @Test
    public void mapearDetallesVentaProducto() {
        Producto producto=quemarProducto();
        DetalleVentaProducto detalleVenta=quemarDetalleVentaProducto(
                "4e20ab9f-3b3b-4bee-95a1-93a64aed9af7",producto.getId());
        List<Object> datosMapeados=ventaProductoRepo.mapearDetalleVentaInverso(detalleVenta);
        if (datosMapeados==null || datosMapeados.size()!=4) {
            fail();
        }
        DetalleVentaProducto detalleVentaMapeada=ventaProductoRepo.mapearDetalleVenta(datosMapeados);
        if (detalleVentaMapeada==null) {
            fail();
        }
        assertAll("Verificar que los datos se mantengan igual despues de los mapeos",
                () -> assertEquals(detalleVenta.getProductoId(),detalleVentaMapeada.getProductoId()),
                () -> assertEquals(detalleVenta.getCantidad(),detalleVentaMapeada.getCantidad()),
                () -> assertEquals(detalleVenta.getValor(),detalleVentaMapeada.getValor()),
                () -> assertEquals(detalleVenta.getVentaId(),detalleVentaMapeada.getVentaId())
        );
    }

    /**
     * Metodo para probar el mapeo de las ventas de productos desde la base de datos
     */
    @Test
    public void mapearVentaProducto() {
        Cuenta cuenta=quemarCuenta();
        Producto producto=quemarProducto();
        VentaProducto ventaProducto=quemarVentaProducto(cuenta.getEmail(),producto.getId());
        List<Object> datosMapeados=ventaProductoRepo.mapearVentaInverso(ventaProducto);
        if (datosMapeados==null || datosMapeados.size()!=14) {
            fail();
        }
        VentaProducto ventaProductoMapeada=ventaProductoRepo.mapearVenta(datosMapeados);
        if (ventaProductoMapeada==null) {
            fail();
        }
        assertAll("Verificar que los datos se mantengan igual despues de los mapeos",
                () -> assertEquals(ventaProducto.getId(),ventaProductoMapeada.getId()),
                () -> assertEquals(ventaProducto.getEmailUsario(),ventaProductoMapeada.getEmailUsario()),
                () -> assertEquals(ventaProducto.getFecha(),ventaProductoMapeada.getFecha()),
                () -> assertEquals(ventaProducto.getTotal(),ventaProductoMapeada.getTotal()),
                () -> assertEquals(ventaProducto.getPromocionId(),ventaProductoMapeada.getPromocionId()),
                () -> assertEquals(ventaProducto.getCodigoPasarela(),
                        ventaProductoMapeada.getCodigoPasarela()),
                () -> assertEquals(ventaProducto.getPago().getId(),
                        ventaProductoMapeada.getPago().getId()),
                () -> assertEquals(ventaProducto.getPago().getCurrency(),
                        ventaProductoMapeada.getPago().getCurrency()),
                () -> assertEquals(ventaProducto.getPago().getPaymentType(),
                        ventaProductoMapeada.getPago().getPaymentType()),
                () -> assertEquals(ventaProducto.getPago().getStatusDetail(),
                        ventaProductoMapeada.getPago().getStatusDetail()),
                () -> assertEquals(ventaProducto.getPago().getAuthorizationCode(),
                        ventaProductoMapeada.getPago().getAuthorizationCode()),
                () -> assertEquals(ventaProducto.getPago().getDate(),
                        ventaProductoMapeada.getPago().getDate()),
                () -> assertEquals(ventaProducto.getPago().getTransactionValue(),
                        ventaProductoMapeada.getPago().getTransactionValue()),
                () -> assertEquals(ventaProducto.getPago().getStatus(),
                        ventaProductoMapeada.getPago().getStatus())
        );
    }

}
