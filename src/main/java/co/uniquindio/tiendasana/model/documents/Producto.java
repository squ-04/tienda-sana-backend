package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.EstadoProducto;
import co.uniquindio.tiendasana.model.enums.CategoriaProducto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("products")
@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

    //Atributos de la clase Producto
    @Id
    @EqualsAndHashCode.Include
    /*esto se traduce como el softr record id no se si se tenga que llamar igual*/
    private String id;

    private String nombre;
    private String descripcion;

    @Getter(AccessLevel.NONE)
    private CategoriaProducto categoria;
    @Getter(AccessLevel.NONE)
    private EstadoProducto estado;
    private int cantidad;
    private String imagen;
    private float precioUnitario;

    /**
     * Metodo constructor para la clase Producto
     * @param nombre
     * @param descripcion
     * @param estado
     * @param categoria
     * @param cantidad
     * @param imagen
     * @param precioUnitario
     */
    @Builder
    public Producto (String nombre, String descripcion, String estado,String categoria, int cantidad, String imagen, float precioUnitario) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria= CategoriaProducto.fromCategoria(categoria);
        this.estado = EstadoProducto.fromEstado(estado);
        this.cantidad = cantidad;
        this.imagen = imagen;
        this.precioUnitario = precioUnitario;
    }

    public String getCategoria(){
        return categoria.getCategoria();
    }

    public String getEstado(){
        return estado.getEstado();
    }

}