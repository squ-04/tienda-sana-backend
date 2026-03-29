package co.uniquindio.tiendasana;

import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.*;
import co.uniquindio.tiendasana.model.enums.*;
import co.uniquindio.tiendasana.model.vo.*;
import co.uniquindio.tiendasana.repos.*;
import co.uniquindio.tiendasana.services.implementations.ProductoServiceImp;
import co.uniquindio.tiendasana.utils.MesaConstantes;
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
    @Autowired
    private MesaRepo mesaRepo;
    @Autowired
    private GestorReservasRepo gestorReservasRepo;
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
        List<Cuenta> cuentas = assertDoesNotThrow(() -> cuentaRepo.obtenerCuentas());
        assertNotNull(cuentas);
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
        List<DetalleCarrito> detalleCarritos = assertDoesNotThrow(() -> carritoComprasRepo.obtenerDetallesCarrito());
        assertNotNull(detalleCarritos);
    }

    /**
     * Metodo para probar la obtencion de carritos de compras sin sus detalles
     */
    @Test
    public void obtenerCarritosComprasSimples() {
        assertDoesNotThrow(() -> {
            List<CarritoCompras> carritoCompras = carritoComprasRepo.obtenerCarritosSimples();
            assertNotNull(carritoCompras);
            for (CarritoCompras carrito : carritoCompras) {
                assertNull(carrito.getProductos());
            }
        });
    }

    /**
     * Metodo para probar la obtencion de detalles de venta de productos
     */
    @Test
    public void obtenerDetallesVentaProducto() {
        List<DetalleVentaProducto> detallesVenta = assertDoesNotThrow(() -> ventaProductoRepo.obtenerDetallesVenta());
        assertNotNull(detallesVenta);
    }

    /**
     * Metodo para probar la obtencion de ventas de productos sin sus detalles
     */
    @Test
    public void obtenerVentaProducto() {
        assertDoesNotThrow(() -> {
            List<VentaProducto> ventasProductos = ventaProductoRepo.obtenerVentasSimples();
            assertNotNull(ventasProductos);
            for (VentaProducto venta : ventasProductos) {
                assertNull(venta.getProductos());
            }
        });
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

    public Mesa quemarMesa() {
        return Mesa.builder()
                .id("K5FztEBeQGUmQQiEz4JJiR")
                .nombre("Mesita")
                .estado(EstadoMesa.DISPONIBLE)
                .localidad(Localidad.CENTRO.getLocalidad())
                .precioReserva(40000)
                .capacidad(4)
                .imagen("https://i.ytimg.com/vi/0KmWjJ8Ip_4/maxresdefault.jpg")
                .idReserva("-")
                .idGestorReserva("-")
                .build();
    }


    public GestorReservas quemarGestorReservas() {
        return GestorReservas.builder()
                .id("1f05f8fd-28e8-447b-aa0a-eade8fe3db38")
                .fecha(LocalDateTime.of(2025, 5, 11, 3, 45))
                .emailUsuario("juanmanuel200413@gmail.com")
                .build();
    }

    /**
     * Metodo para probar el mapeo de mesas desde la base de datos
     */
    @Test
    public void mapearMesa() {
        Mesa mesa=quemarMesa();
        List<Object> datosMapeados=mesaRepo.mapearMesaInverso(mesa);
        if (datosMapeados==null || datosMapeados.size()!=9) {
            fail();
        }
        Mesa mesaMapeada=mesaRepo.mapearMesa(datosMapeados, MesaConstantes.HOJA_CLIENTE);
        if (mesaMapeada==null) {
            fail();
        }
        assertAll("Verificar que los datos se mantengan igual despues de los mapeos",
                () -> assertEquals(mesa.getId(),mesaMapeada.getId()),
                () -> assertEquals(mesa.getNombre(),mesaMapeada.getNombre()),
                () -> assertEquals(mesa.getEstado(),mesaMapeada.getEstado()),
                () -> assertEquals(mesa.getLocalidad(),mesaMapeada.getLocalidad()),
                () -> assertEquals(mesa.getPrecioReserva(),mesaMapeada.getPrecioReserva()),
                () -> assertEquals(mesa.getCapacidad(),mesaMapeada.getCapacidad()),
                () -> assertEquals(mesa.getImagen(),mesaMapeada.getImagen()),
                () -> assertEquals(mesa.getIdReserva(),mesaMapeada.getIdReserva()),
                () -> assertEquals(mesa.getIdGestorReserva(),mesaMapeada.getIdGestorReserva())
        );
    }

    /**
     * Metodo para probar el mapeo de gestores de mesas desde la base de datos
     */
    @Test
    public void mapearGestorReservas() {
        GestorReservas gestorReservas=quemarGestorReservas();
        List<Object> datosMapeados=gestorReservasRepo.mapearGestorReservasInverso(gestorReservas);
        if (datosMapeados==null || datosMapeados.size()!=3) {
            fail();
        }
        GestorReservas gestorReservasMapeado=gestorReservasRepo.mapearGestorReservas(datosMapeados);
        if (gestorReservasMapeado==null) {
            fail();
        }
        assertAll("Verificar que los datos se mantengan igual despues de los mapeos",
                () -> assertEquals(gestorReservas.getId(),gestorReservasMapeado.getId()),
                () -> assertEquals(gestorReservas.getFecha(),gestorReservasMapeado.getFecha()),
                () -> assertEquals(gestorReservas.getEmailUsuario(),gestorReservasMapeado.getEmailUsuario())
        );
    }

}
