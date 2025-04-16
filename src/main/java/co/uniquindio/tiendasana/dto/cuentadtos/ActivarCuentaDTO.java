package co.uniquindio.tiendasana.dto.cuentadtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data transfer object para recibir informacion de activacion de la cuenta
 * @param email
 * @param codigoVerificacionRegistro
 */
public record ActivarCuentaDTO (
        @NotBlank(message = "Campo Email no puede estar vacio")
        @Email(message = "Formato de email invalido")
        String email,
        @NotBlank(message = "Campo codigo no puede star vacio")
        @Size(min = 6, max = 6, message = "El codigo de validacion debe de tener 6 caracteres de tamaño")
        String codigoVerificacionRegistro
) {
}
