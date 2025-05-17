package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.CategoriaProducto;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.Localidad;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mesa {

    @EqualsAndHashCode.Include
    private String id;
    private String nombre;

    @Getter(AccessLevel.NONE)
    private EstadoMesa estado;

    private Localidad localidad;
    private float precioReserva;
    private int capacidad;
    private String imagen;


    @Builder
    public Mesa(String id, String nombre, EstadoMesa estado, String localidad, float precioReserva, int capacidad, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
        this.localidad = Localidad.fromLocalidad(localidad);
        this.precioReserva = precioReserva;
        this.capacidad = capacidad;
        this.imagen = imagen;
    }

    public String getEstado (){
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

