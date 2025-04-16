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
/**
 * Controlador relacionado con las acciones de autenticación accesibles para todos
 */
public class AutenticacionController {

    private final CuentaService cuentaService;

    /**
     * Controlador para recibir la intruccion de loging, los datos y devolver un token
     * @param loginDTO
     * @return Token de inicio de sesión
     * @throws Exception
     */
    @PostMapping("/login")
    public ResponseEntity<MessageDTO<TokenDTO>> login(@Valid @RequestBody LoginDTO loginDTO) throws Exception{
        TokenDTO token = cuentaService.login(loginDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, token));
    }

    public ResponseEntity<MessageDTO<TokenDTO>> refresh (Map<String,Object> claims) throws Exception{
        TokenDTO token = cuentaService.refresh(claims);
        return ResponseEntity.ok(new MessageDTO<>(true, token));
    }

    /**
     * Controlador para crear una cuenta de un usuario dada cierta informacion
     * @param cuenta (DTO con la informacion para crear la cuenta)
     * @return
     * @throws Exception
     */
    @PostMapping("/create-account")
    public ResponseEntity<MessageDTO<String>> crearCuenta(@Valid @RequestBody CrearCuentaDTO cuenta) throws Exception {
        String accountId= cuentaService.crearCuenta(cuenta);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    /**
     * Controlador para enviar un codigo de recuperación de contraseña al correo de la cuenta
     * @param email
     * @return
     * @throws Exception
     */
    @PutMapping("/send-recover/{email}")
    public ResponseEntity<MessageDTO<String>> enviarCodigoRecuparacionContrasenia(@PathVariable String email) throws Exception {
        String accountId= cuentaService.enviarCodigoRecuperacion(email);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    /**
     * Controlador para hacer el cambio de contraseña dada la informacion del codigo, nueva contraseña y el correo
     * @param cambiarContraseniaDTO
     * @return
     * @throws Exception
     */
    @PutMapping("/change-password")
    public ResponseEntity<MessageDTO<String>> cambiarCodigoContrasenia
            (@Valid @RequestBody CambiarContraseniaDTO cambiarContraseniaDTO) throws Exception {
        String accountId= cuentaService.cambiarContrasenia(cambiarContraseniaDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    /**
     * Controlador para validar una cuenta y darle el estado a activa dado el codigo de activacion
     * @param activarCuentaDTO
     * @return
     * @throws Exception
     */
    @PutMapping("/validate-account")
    public ResponseEntity<MessageDTO<String>>validateRegistrationCode(@Valid @RequestBody ActivarCuentaDTO activarCuentaDTO) throws Exception{
        String accountId= cuentaService.validarCodigoRegistro(activarCuentaDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

    /**
     * Controlador para reenviar el codigo de validacion de una cuenta y que el usuario pueda activarla
     * @param email
     * @return
     * @throws Exception
     */
    @PutMapping("/resend-validation/{email}")
    public ResponseEntity<MessageDTO<String>> reassignValidationRegistrationCode(@PathVariable String email) throws Exception{
        String accountId= cuentaService.reenviarCodigoRegistro(email);
        return ResponseEntity.ok(new MessageDTO<>(false, accountId));
    }

}
