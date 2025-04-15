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

    /**
     *  Endporint mediante el cual se obtienen los productos que verán los clientes
     * @return
     * @throws Exception
     */
    /**
    @GetMapping("/productos/get-all/{pagina}")
    public ResponseEntity<MessageDTO<List<ProductoItemDTO>>> listarProductosCliente(@PathVariable int pagina) throws Exception {
        List<ProductoItemDTO> productos= productService.obtenerProductosCliente(pagina);
        return ResponseEntity.ok( new MessageDTO<>(false, productos));
    }
     */
    @GetMapping("/event/get-all/{page}")
    public ResponseEntity<MessageDTO<ListaProductos>> listaProductosCliente(@PathVariable int pagina){
        ListaProductos productos = null;
        try {
            productos = productService.obtenerProductosCliente(pagina);
            return ResponseEntity.ok(new MessageDTO<>(false,productos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new MessageDTO<>(true,productos));
        }
    }

    @GetMapping("/event/get-info/{id}")
    public ResponseEntity<MessageDTO<ProductoInfoDTO>> getInfoEvenClient(@PathVariable String id) throws Exception{
        ProductoInfoDTO productoInfo = productService.obtenerInfoProducto(id);
        return ResponseEntity.ok(new MessageDTO<>(false,productoInfo));
    }

}
