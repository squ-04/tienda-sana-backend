package co.uniquindio.tiendasana.controllers.admin;

import co.uniquindio.tiendasana.dto.admin.SupplierRequest;
import co.uniquindio.tiendasana.dto.admin.SupplierResponse;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.services.admin.AdminSupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSupplierController {

    private final AdminSupplierService supplierService;

    @GetMapping
    public ResponseEntity<MessageDTO<List<SupplierResponse>>> list() {
        return ResponseEntity.ok(new MessageDTO<>(false, supplierService.listAll()));
    }

    @PostMapping
    public ResponseEntity<MessageDTO<SupplierResponse>> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, supplierService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageDTO<SupplierResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, supplierService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDTO<String>> deactivate(@PathVariable String id) {
        supplierService.deactivate(id);
        return ResponseEntity.ok(new MessageDTO<>(false, "Proveedor desactivado"));
    }
}
