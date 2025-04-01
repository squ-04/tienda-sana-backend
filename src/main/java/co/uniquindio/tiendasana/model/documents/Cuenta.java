package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.Usuario;
import co.uniquindio.tiendasana.model.vo.CodigoValidacion;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cuenta {
    //Main attribute for identification
    @EqualsAndHashCode.Include
    private String id;
    //Secundary attributes for the class
    private Usuario usuario;
    private String email;
    private String contrasenia;
    private EstadoCuenta estado;
    private Rol rol;
    private LocalDateTime fechaRegistro;

    //Attributes are for security on the accounts
    private CodigoValidacion codigoValidacionRegistro;
    private CodigoValidacion codigoValidacionContrasenia;

    @Builder
    private Cuenta(Usuario usuario, String email, String contrasenia, Rol rol, LocalDateTime fechaRegistro,
                   EstadoCuenta estado, CodigoValidacion codigoValidacionRegistro, CodigoValidacion codigoValidacionContrasenia) {
        this.usuario = usuario;
        this.email = email;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
        this.codigoValidacionRegistro = codigoValidacionRegistro;
        this.codigoValidacionContrasenia = codigoValidacionContrasenia;

    }
}