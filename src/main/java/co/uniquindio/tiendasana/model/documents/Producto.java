package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.EstadoProducto;
import co.uniquindio.tiendasana.model.enums.CategoriaProducto;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

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
    private int calificacionPromedio;
    private float precioUnitario;

    /**
     * Texto de categoría tal como se almacena en Mongo (catálogo admin con categorías libres).
     * Si está presente, {@link #getCategoria()} lo devuelve en lugar del enum.
     */
    private String categoriaLibre;

    /**
     * Texto de estado para vistas; si está presente tiene prioridad sobre el enum.
     */
    private String estadoLibre;

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
    public Producto (String nombre, String descripcion, String estado,String categoria, int cantidad, String imagen, float precioUnitario, String id) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria= CategoriaProducto.fromCategoria(categoria);
        this.estado = EstadoProducto.fromEstado(estado);
        this.cantidad = cantidad;
        this.imagen = imagen;
        this.precioUnitario = precioUnitario;
        this.id = id;
    }

    public String getCategoria() {
        if (categoriaLibre != null && !categoriaLibre.isBlank()) {
            return categoriaLibre;
        }
        return categoria != null ? categoria.getCategoria() : "";
    }

    public String getEstado() {
        if (estadoLibre != null && !estadoLibre.isBlank()) {
            return estadoLibre;
        }
        return estado != null ? estado.getEstado() : "";
    }

    public boolean estaStockDisponible(int cantidadComprar) {
        return getCantidad()-cantidadComprar>=0;
    }
}