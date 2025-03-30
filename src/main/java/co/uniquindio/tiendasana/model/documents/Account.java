package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.Usuario;
import co.uniquindio.tiendasana.model.vo.CodigoValidacion;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("accounts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Account {
    //Main attribute for identification
    @Id
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
    private CodigoValidacion codigoVerificacion;
    private CodigoValidacion validacionContrasenia;

    @Builder
    private Account (Usuario usuario, String email, String contrasenia, Rol rol, LocalDateTime fechaRegistro,
                     EstadoCuenta estado, CodigoValidacion codigoVerificacion, CodigoValidacion validacionContrasenia) {
        this.usuario = usuario;
        this.email = email;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
        this.codigoVerificacion = codigoVerificacion;
        this.validacionContrasenia = validacionContrasenia;

    }
}