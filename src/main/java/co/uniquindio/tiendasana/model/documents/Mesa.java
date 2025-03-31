package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("tables")
@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mesa {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String nombre;

    @Getter(AccessLevel.NONE)
    private EstadoMesa estado;

    private String localidad;
    private float precioReserva;

    @Builder
    public Mesa(String nombre, EstadoMesa estado, String localidad, float precioReserva) {
        this.nombre = nombre;
        this.estado = estado;
        this.localidad = localidad;
        this.precioReserva = precioReserva;
    }

    public String getStado (){
        return estado.getEstado();
    }
}



/*Clase por hecha por el hijo de Robinson

package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("tables")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mesa {
    @Id
    @EqualsAndHashCode.Include
    private int id;
    @EqualsAndHashCode.Include
    private int number;
    private EstadoMesa status;

    @Builder
    public Mesa(int number, EstadoMesa status) {
        this.number = number;
        this.status = status;
    }
}
 */

