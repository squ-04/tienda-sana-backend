package co.uniquindio.tiendasana.dto.cuentadtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContraseniaDTO (
        @NotBlank(message = "Campo Email no puede estar vacio")
        @Email(message = "Formato de email invalido")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 7, max = 20, message = "La contraseña debe de tener entre 7 y 20 caracteres")
        String nuevaContrasenia,
        @NotBlank(message = "Campo codigo no puede star vacio")
        @Size(min = 6, max = 6, message = "El codigo de validacion para la " +
                "contraseña debe de tener 6 caracteres de tamaño")
        String codigoVerificacion
){
}
