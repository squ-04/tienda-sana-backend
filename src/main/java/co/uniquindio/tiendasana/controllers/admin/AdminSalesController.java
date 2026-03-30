package co.uniquindio.tiendasana.controllers.admin;

import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.services.interfaces.VentaProductoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sales")
@SecurityRequirement(name = "bearerAuth")
public class AdminSalesController {

    private final VentaProductoService ventaProductoService;

    @PostMapping("/{ventaProductoId}/refund")
    public ResponseEntity<MessageDTO<String>> solicitarReembolsoComoAdmin(@PathVariable String ventaProductoId) {
        try {
            String result = ventaProductoService.solicitarReembolsoVenta(ventaProductoId, null, true);
            return ResponseEntity.ok(new MessageDTO<>(false, result));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageDTO<>(true, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageDTO<>(true, "Error al solicitar reembolso como admin: " + e.getMessage()));
        }
    }
}
