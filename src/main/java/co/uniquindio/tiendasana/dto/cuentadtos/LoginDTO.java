package co.uniquindio.tiendasana.dto.cuentadtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data transfer object para obtener informacion del login y poder hacer el proceso de retornar un token de sesión
 * @param email
 * @param contrasenia
 */
public record LoginDTO(
        @NotBlank(message = "Campo Email no puede estar vacio")
        @Email(message = "Formato de email invalido")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 7, max = 20, message = "La contraseña debe de tener entre 7 y 20 caracteres")
        String contrasenia
) {
}
