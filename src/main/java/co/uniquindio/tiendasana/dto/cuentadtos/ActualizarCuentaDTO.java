package co.uniquindio.tiendasana.dto.cuentadtos;

/**
 * Data transfer object para pasar la información
 * @param nombre
 * @param telefono
 * @param direccion
 * @param contrasenia
 */
public record ActualizarCuentaDTO (
        String correo,
        String nombre,
        String telefono,
        String direccion,
        String contrasenia
){
}
