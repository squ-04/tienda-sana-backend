package co.uniquindio.tiendasana.model.vo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Usuario {
    private String dni;
    private String nombre;
    private String direccion;
    private String telefono;

    @Builder
    private Usuario(String dni, String nombre, String direccion, String telefono) {
        this.dni = dni;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }
}
