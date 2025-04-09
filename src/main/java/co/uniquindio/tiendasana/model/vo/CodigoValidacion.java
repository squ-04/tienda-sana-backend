package co.uniquindio.tiendasana.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CodigoValidacion {


    //Attributes for the class
    @EqualsAndHashCode.Include
    private String codigo;
    private LocalDateTime fechaCreacion;

    //Constructor method for the class
    @Builder
    public CodigoValidacion(LocalDateTime fechaCreacion, String codigo) {
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
    }
}