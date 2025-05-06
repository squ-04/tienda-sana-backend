package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.TokenDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.CambiarContraseniaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.CrearCuentaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.LoginDTO;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.dto.mesadtos.ListaMesas;
import co.uniquindio.tiendasana.dto.productodtos.ListaProductos;
import co.uniquindio.tiendasana.dto.productodtos.ProductoInfoDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoItemDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.services.implementations.ReservaServiceImp;
import co.uniquindio.tiendasana.services.interfaces.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controlador para los endpoints publicos, es decir no necesitan de un JWT con rol para ser accedidos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicoController {

    /**
     * Instacia del servicio para los métodos de los productos
     */
    private final ProductoService productService;
    private final CuentaService cuentaService;
    private final VentaProductoService ventaProductoService;
    private final ReservaService reservaService;
    private final MesaService mesaService;

    /**
     *  Endporint mediante el cual se obtienen los productos que verán los clientes
     * @param pagina Pagina de los productos
     * @return Respuesta a la solicitud
     * @throws Exception
     */
    @GetMapping("/productos/get-all/{pagina}")
    public ResponseEntity<MessageDTO<ListaProductos>> listarProductosCliente(@PathVariable int pagina) throws Exception {
        ListaProductos productos= productService.obtenerProductosCliente(pagina);
        return ResponseEntity.ok( new MessageDTO<>(false, productos));
    }

    /**
     * Endpoint mediante el cual se obtiene las mesas que verán los clientes
     * @param pagina Pagina de las mesas
     * @return Respuesta a la solicitud
     * @throws Exception
     */
    @GetMapping("/mesas/get-all")
    public ResponseEntity<MessageDTO<ListaMesas>> listarMesasCliente(@PathVariable int pagina) throws Exception {
        ListaMesas mesas= mesaService.obtenerMesasCliente(pagina);
        return ResponseEntity.ok( new MessageDTO<>(false, mesas));
    }

    /**
     * Endpoint mediante el cual se recibe la notificación de Mercado Pago para ventas
     * @param request Datos para poder recibir notificacion
     * @return Respuesta a la solicitud
     */
    @PostMapping("/venta/receive-notification")
    public ResponseEntity<MessageDTO<String>> receiveNotificationFromMercadoPago(@RequestBody Map<String, Object> request){
        ventaProductoService.receiveNotificationFromMercadoPago(request);
        return ResponseEntity.ok(new MessageDTO<>(false,"Notification received"));
    }


    /**
     * Endpoint mediante el cual se recibe la notificación de Mercado Pago para reservas
     * @param request Datos para poder recibir notificacion
     * @return Respuesta a la solicitud
     */
    @PostMapping("/reserva/receive-notification")
    public ResponseEntity<MessageDTO<String>> receiveNotificationFromMercadoPagoReserva(@RequestBody Map<String, Object> request){
        reservaService.receiveNotificationFromMercadoPago(request);
        return ResponseEntity.ok(new MessageDTO<>(false,"Notification received"));
    }

    /**
     * Endpoint mediante el cual se obtiene la información de un producto
     * @param id Id del producto
     * @return Respuesta a la solicitud
     * @throws Exception
     */
    @GetMapping("/productos/get-info/{id}")
    public ResponseEntity<MessageDTO<ProductoInfoDTO>> getInfoEvenClient(@PathVariable String id) throws Exception{
        ProductoInfoDTO productoInfo = productService.obtenerInfoProducto(id);
        return ResponseEntity.ok(new MessageDTO<>(false,productoInfo));
    }

}
