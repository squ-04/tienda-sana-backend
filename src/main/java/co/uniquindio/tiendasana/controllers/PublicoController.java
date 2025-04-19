package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.TokenDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.CambiarContraseniaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.CrearCuentaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.LoginDTO;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.dto.productodtos.ListaProductos;
import co.uniquindio.tiendasana.dto.productodtos.ProductoInfoDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoItemDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.services.interfaces.CuentaService;
import co.uniquindio.tiendasana.services.interfaces.ProductoService;
import co.uniquindio.tiendasana.services.interfaces.VentaProductoService;
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

    /**
     *  Endporint mediante el cual se obtienen los productos que verán los clientes
     * @return
     * @throws Exception
     */

    @GetMapping("/productos/get-all/{pagina}")
    public ResponseEntity<MessageDTO<ListaProductos>> listarProductosCliente(@PathVariable int pagina) throws Exception {
        ListaProductos productos= productService.obtenerProductosCliente(pagina);
        return ResponseEntity.ok( new MessageDTO<>(false, productos));
    }

    /**
     * Endpoint mediante el cual se recibe la notificación de Mercado Pago
     * @param request
     * @return
     */
    @PostMapping("/venta/receive-notification")
    public ResponseEntity<MessageDTO<String>> receiveNotificationFromMercadoPago(@RequestBody Map<String, Object> request){
        ventaProductoService.receiveNotificationFromMercadoPago(request);
        return ResponseEntity.ok(new MessageDTO<>(false,"Notification received"));
    }

    /**
     * Endpoint mediante el cual se obtiene la información de un producto
     * @param id Id del producto
     * @return
     * @throws Exception
     */
    @GetMapping("/productos/get-info/{id}")
    public ResponseEntity<MessageDTO<ProductoInfoDTO>> getInfoEvenClient(@PathVariable String id) throws Exception{
        ProductoInfoDTO productoInfo = productService.obtenerInfoProducto(id);
        return ResponseEntity.ok(new MessageDTO<>(false,productoInfo));
    }

}
