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

import co.uniquindio.tiendasana.dto.cuentadtos.ActualizarCuentaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.CambiarMiContraseniaDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.InfoCuentaDTO;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.services.interfaces.CuentaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * Método auxiliar para obtener el email del usuario autenticado desde el contexto de seguridad.
     * @return Email del usuario autenticado.
     * @throws AccessDeniedException si no hay un usuario autenticado.
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Usuario no autenticado. Se requiere iniciar sesión.");
        }
        // Asumimos que authentication.getName() devuelve el email configurado en tu UserDetailsService/JWT filter.
        return authentication.getName();
    }


    /**
     * Controlador para actualizar los datos de una cuenta
     * @param cuenta Datos para actualizar la cuenta
     * @return Respuesta a la solicitud
     * @throws Exception
     */
    @PutMapping("/update-account")
    public ResponseEntity<MessageDTO<String>> actualizarCuenta(@Valid @RequestBody ActualizarCuentaDTO cuenta) throws Exception {
        try {
            String emailAutenticado = getAuthenticatedUserEmail();

            if (!emailAutenticado.equals(cuenta.email())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageDTO<>(true, "No tiene permisos para modificar los datos de otra cuenta."));
            }

            String resultado = cuentaService.actualizarCuenta(cuenta, emailAutenticado);
            return ResponseEntity.ok(new MessageDTO<>(false, resultado));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO<>(true, e.getMessage()));
        } catch (Exception e) {
            // Considera loggear el error e.printStackTrace(); o usar un logger dedicado.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageDTO<>(true, "Error al actualizar la cuenta: " + e.getMessage()));
        }
    }

    /**
     * Controlador para eliminar (Lógicamente) una cuenta
     * @param email Email de la cuenta
     * @return Respuesta a la solicitud
     * @throws Exception
     */
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<MessageDTO<String>> eliminarCuenta(@PathVariable String email) throws Exception {
        try {
            String emailAutenticado = getAuthenticatedUserEmail();
            String resultado = cuentaService.eliminarCuenta(email, emailAutenticado);
            return ResponseEntity.ok(new MessageDTO<>(false, resultado));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO<>(true, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageDTO<>(true, "Error al eliminar la cuenta: " + e.getMessage()));
        }
    }

    /**
     * Controlador para obtener la informacion relacionada con una cuenta dado su email (llave principal)
     * @param email Email de la cuenta
     * @return Respuesta a la solicitud
     * @throws Exception
     */
    @GetMapping("/get/{email}")
    public ResponseEntity<MessageDTO<InfoCuentaDTO>> obtenerInfoCuenta(@PathVariable String email) throws Exception {
        try {
            String emailAutenticado = getAuthenticatedUserEmail();
            InfoCuentaDTO info = cuentaService.obtenerInfoCuenta(email, emailAutenticado);
            return ResponseEntity.ok(new MessageDTO<>(false, info));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageDTO<>(true, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageDTO<InfoCuentaDTO>(true, null));
        }
    }

}
