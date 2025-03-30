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
    private CodigoValidacion(String codigo, LocalDateTime fechaCreacion) {
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
    }
}