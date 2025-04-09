package co.uniquindio.tiendasana.dto.cuentadtos;

public record CambiarContraseniaDTO (
        String email,
        String nuevaContrasenia,
        String codigoVerificacion
){
}
