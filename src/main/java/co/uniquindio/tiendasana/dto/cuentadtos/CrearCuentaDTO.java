package co.uniquindio.tiendasana.dto.cuentadtos;

public record CrearCuentaDTO(
        String nombre,
        String dni,
        String telefono,
        String direccion,
        String email,
        String contrasenia,
        String confirmacionContrasenia
) {
}
