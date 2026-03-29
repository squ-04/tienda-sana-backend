package co.uniquindio.tiendasana.controllers.admin;

import co.uniquindio.tiendasana.dto.admin.InventoryResponse;
import co.uniquindio.tiendasana.dto.admin.ProductLotRequest;
import co.uniquindio.tiendasana.dto.admin.ProductLotResponse;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.services.admin.AdminProductLotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductLotController {

    private final AdminProductLotService lotService;

    @GetMapping("/lots")
    public ResponseEntity<MessageDTO<List<ProductLotResponse>>> listLots(
            @RequestParam(required = false) String productId) {
        return ResponseEntity.ok(new MessageDTO<>(false, lotService.listAll(productId)));
    }

    @PostMapping("/lots")
    public ResponseEntity<MessageDTO<ProductLotResponse>> create(@Valid @RequestBody ProductLotRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, lotService.create(request)));
    }

    @PutMapping("/lots/{id}")
    public ResponseEntity<MessageDTO<ProductLotResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody ProductLotRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, lotService.update(id, request)));
    }

    @DeleteMapping("/lots/{id}")
    public ResponseEntity<MessageDTO<String>> deleteLot(@PathVariable String id) {
        lotService.delete(id);
        return ResponseEntity.ok(new MessageDTO<>(false, "Lote eliminado"));
    }

    @GetMapping("/inventory")
    public ResponseEntity<MessageDTO<List<InventoryResponse>>> inventory() {
        return ResponseEntity.ok(new MessageDTO<>(false, lotService.inventory()));
    }
}
