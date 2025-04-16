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
/**
 * Controlador para las acciones relacionadas con una cuenta que ya está logueada
 */
public class CuentaController {

    private final CuentaService cuentaService;

    /**
     * Controlador para actualizar los datos de una cuenta
     * @param cuenta
     * @return
     * @throws Exception
     */
    @PutMapping("/update-account")
    public ResponseEntity<MessageDTO<String>> actualizarCuenta(@Valid @RequestBody ActualizarCuentaDTO cuenta) throws Exception {
        String accountId=cuentaService.actualizarCuenta(cuenta);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    /**
     * Controlador para eliminar (Lógicamente) una cuenta
     * @param email
     * @return
     * @throws Exception
     */
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<MessageDTO<String>> eliminarCuenta(@PathVariable String email) throws Exception {
        String accountId= cuentaService.eliminarCuenta(email);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    /**
     * Controlador para obtener la informacion relacionada con una cuenta dado su email (llave principal)
     * @param email
     * @return
     * @throws Exception
     */
    @GetMapping("/get/{email}")
    public ResponseEntity<MessageDTO<InfoCuentaDTO>> obtenerInfoCuenta(@PathVariable String email) throws Exception {
        InfoCuentaDTO info = cuentaService.obtenerInfoCuenta(email);
        return ResponseEntity.ok(new MessageDTO<>(false, info));
    }

}
