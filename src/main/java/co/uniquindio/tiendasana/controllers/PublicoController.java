package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoItemDTO;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.services.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final ProductService productService;

    /**
     *  Endporint mediante el cual se obtienen los productos que verán los clientes
     * @return
     * @throws Exception
     */
    @GetMapping("/productos/get-all")
    public ResponseEntity<MessageDTO<List<ProductoItemDTO>>> listarProductosCliente() throws Exception {
        List<ProductoItemDTO> productos= productService.obtenerProductosCliente();
        return ResponseEntity.ok( new MessageDTO<>(false, productos));
    }
}
