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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicoController {

    private final ProductService productService;

    @GetMapping("/productos/get-all")
    public ResponseEntity<MessageDTO<List<ProductoItemDTO>>> listarProductosCliente() throws Exception {
        List<ProductoItemDTO> productos= productService.obtenerProductosCliente();
        return ResponseEntity.ok( new MessageDTO<>(false, productos));
    }
}
