package co.uniquindio.tiendasana.controllers.admin;

import co.uniquindio.tiendasana.dto.admin.AdminProductRequest;
import co.uniquindio.tiendasana.dto.admin.AdminProductResponse;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.services.admin.AdminProductCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductCatalogService catalogService;

    @GetMapping
    public ResponseEntity<MessageDTO<List<AdminProductResponse>>> list() {
        return ResponseEntity.ok(new MessageDTO<>(false, catalogService.listAllForAdmin()));
    }

    @PostMapping
    public ResponseEntity<MessageDTO<AdminProductResponse>> create(@Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, catalogService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageDTO<AdminProductResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, catalogService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDTO<String>> deactivate(@PathVariable String id) {
        catalogService.deactivate(id);
        return ResponseEntity.ok(new MessageDTO<>(false, "Producto desactivado"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageDTO<AdminProductResponse>> toggleStatus(@PathVariable String id) {
        return ResponseEntity.ok(new MessageDTO<>(false, catalogService.toggleActive(id)));
    }
}
