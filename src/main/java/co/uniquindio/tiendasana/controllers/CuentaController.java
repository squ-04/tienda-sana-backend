package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.cuentadtos.ActualizarCuentaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.InfoCuentaDTO;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.services.interfaces.CuentaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")

@SecurityRequirement(name = "bearerAuth")
public class CuentaController {

    private final CuentaService cuentaService;

    @PutMapping("/update-account")
    public ResponseEntity<MessageDTO<String>> actualizarCuenta(@Valid @RequestBody ActualizarCuentaDTO cuenta) throws Exception {
        String accountId=cuentaService.actualizarCuenta(cuenta);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<MessageDTO<String>> eliminarCuenta(@PathVariable String email) throws Exception {
        String accountId= cuentaService.eliminarCuenta(email);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<MessageDTO<InfoCuentaDTO>> obtenerInfoCuenta(@PathVariable String email) throws Exception {
        InfoCuentaDTO info = cuentaService.obtenerInfoCuenta(email);
        return ResponseEntity.ok(new MessageDTO<>(false, info));
    }

}
