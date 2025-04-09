package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.services.interfaces.ProductoService;
import co.uniquindio.tiendasana.services.interfaces.MesaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private ProductoService productService;
    private MesaService tableService;
    /*
    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> createUpdateProduct(@Valid @RequestBody productDTO product){
        productService.createUpdateProduct(product);
        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> deleteProduct(@Valid @RequestBody productDTO product){
        productService.deleteProduct(product);
        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> createUpdatePTable(@Valid @RequestBody tableDTO table){
        tableService.createUpdatePTable(table);
        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> deleteTable(@Valid @RequestBody tableDTO table){
        tableService.deleteTable(table);
        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> createUpdateLocation(@Valid @RequestBody locationDTO location){
        locationService.createUpdateLocation(location);
        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> deleteLocation(@Valid @RequestBody locationDTO location){
        locationService.deleteLocation(location);
        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }
    */
}
