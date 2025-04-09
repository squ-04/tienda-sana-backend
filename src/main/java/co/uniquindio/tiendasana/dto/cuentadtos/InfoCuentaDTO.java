package co.uniquindio.tiendasana.dto.cuentadtos;

import co.uniquindio.tiendasana.model.enums.Rol;

public record InfoCuentaDTO(
        String id,
        String nombre,
        String cedula,
        String telefono,
        String direccion,
        String email,
        Rol rol
) {
}
