package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoItemDTO;
import co.uniquindio.tiendasana.services.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     *  Endporint mediante el cual se obtienen los productos que verán los clientes
     * @return
     * @throws Exception
     */
    @GetMapping("/productos/get-all/{pagina}")
    public ResponseEntity<MessageDTO<List<ProductoItemDTO>>> listarProductosCliente(@PathVariable int pagina) throws Exception {
        List<ProductoItemDTO> productos= productService.obtenerProductosCliente(pagina);
        return ResponseEntity.ok( new MessageDTO<>(false, productos));
    }
}
