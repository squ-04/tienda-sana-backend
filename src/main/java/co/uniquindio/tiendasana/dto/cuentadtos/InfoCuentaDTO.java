package co.uniquindio.tiendasana.dto.cuentadtos;

import co.uniquindio.tiendasana.model.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record InfoCuentaDTO(
        //This id is gonna be a token in the future
        @NotNull(message = "ID cannot be null")
                String id,

        @NotBlank(message = "The DNI is mandatory")
        @Length(max = 10, message = "The DNI must have a maximum of 10 characters")
        String dni,

        @NotBlank(message = "The name cannot be empty")
        @Size(min = 1, max = 100, message = "The name must be between 1 and 100 characters")
        String nombre,

        @Size(min = 10, max = 15, message = "The phone number must be between 10 and 15 characters long")
        String telefono,

        @Size(max = 255, message = "The address cannot exceed 255 characters")
        String direccion,

        @NotBlank(message = "Email can't be empty")
        @Email(message = "Email format is invalid")
        String email
) {
}
