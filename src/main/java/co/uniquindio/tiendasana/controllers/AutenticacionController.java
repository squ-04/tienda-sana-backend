package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.TokenDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.ActivarCuentaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.CambiarContraseniaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.CrearCuentaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.LoginDTO;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.services.interfaces.CuentaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final CuentaService cuentaService;

    @PostMapping("/login")
    public ResponseEntity<MessageDTO<TokenDTO>> login(@Valid @RequestBody LoginDTO loginDTO) throws Exception{
        TokenDTO token = cuentaService.login(loginDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, token));
    }

    public ResponseEntity<MessageDTO<TokenDTO>> refresh (Map<String,Object> claims) throws Exception{
        TokenDTO token = cuentaService.refresh(claims);
        return ResponseEntity.ok(new MessageDTO<>(true, token));
    }

    @PostMapping("/create-account")
    public ResponseEntity<MessageDTO<String>> crearCuenta(@Valid @RequestBody CrearCuentaDTO cuenta) throws Exception {
        String accountId= cuentaService.crearCuenta(cuenta);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    @PutMapping("/send-recover/{email}")
    public ResponseEntity<MessageDTO<String>> enviarCodigoRecuparacionContrasenia(@PathVariable String email) throws Exception {
        String accountId= cuentaService.enviarCodigoRecuperacion(email);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    @PutMapping("/change-password")
    public ResponseEntity<MessageDTO<String>> cambiarCodigoContrasenia
            (@Valid @RequestBody CambiarContraseniaDTO cambiarContraseniaDTO) throws Exception {
        String accountId= cuentaService.cambiarContrasenia(cambiarContraseniaDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    @PutMapping("/validate-account")
    public ResponseEntity<MessageDTO<String>>validateRegistrationCode(@Valid @RequestBody ActivarCuentaDTO activarCuentaDTO) throws Exception{
        String accountId= cuentaService.validarCodigoRegistro(activarCuentaDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    @PutMapping("/resend-validation/{email}")
    public ResponseEntity<MessageDTO<String>> reassignValidationRegistrationCode(@PathVariable String email) throws Exception{
        String accountId= cuentaService.reenviarCodigoRegistro(email);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

}
