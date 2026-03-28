package co.uniquindio.tiendasana.controllers.admin;

import co.uniquindio.tiendasana.dto.admin.RestaurantTableRequest;
import co.uniquindio.tiendasana.dto.admin.RestaurantTableResponse;
import co.uniquindio.tiendasana.dto.admin.TableStatusPatchRequest;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.services.admin.AdminRestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestaurantTableController {

    private final AdminRestaurantTableService tableService;

    @GetMapping
    public ResponseEntity<MessageDTO<List<RestaurantTableResponse>>> list() {
        return ResponseEntity.ok(new MessageDTO<>(false, tableService.listAll()));
    }

    @PostMapping
    public ResponseEntity<MessageDTO<RestaurantTableResponse>> create(@Valid @RequestBody RestaurantTableRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, tableService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageDTO<RestaurantTableResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody RestaurantTableRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, tableService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageDTO<RestaurantTableResponse>> patchStatus(
            @PathVariable String id,
            @Valid @RequestBody TableStatusPatchRequest request) {
        return ResponseEntity.ok(new MessageDTO<>(false, tableService.patchStatus(id, request)));
    }
}
