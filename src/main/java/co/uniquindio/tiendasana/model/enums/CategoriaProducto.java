package co.uniquindio.tiendasana.model.enums;

import lombok.Getter;

/**
 * Enum para las categorias de los productos
 */
@Getter
public enum CategoriaProducto {

    BEBIDA ("Bebida"),
    CEREALES("Cereales"),
    FRUTAS ("Frutas"),
    FRUTOSSECOS("Frutos secos"),
    LACTEOS ("Lacteos"),
    PANADERIA ("Panadería"),
    TE("Té"),
    /** Categoría genérica para valores que no encajan en el catálogo legacy (p. ej. productos creados desde admin). */
    OTROS("Otros");

    private final String categoria;

    /**
     * Metodo contructor de las categorias
     * @param categoria
     */
     CategoriaProducto(String categoria) {
         this.categoria = categoria;
     }

    /**
     * Metodo que dada una cadena retorna un valor del enum
     * @param categoria
     * @return
     */
    public static CategoriaProducto fromCategoria(String categoria) {
        for (CategoriaProducto categoriaProducto : CategoriaProducto.values()) {
            if (categoriaProducto.getCategoria().equalsIgnoreCase(categoria)) {
                return categoriaProducto;
            }
        }
        throw new IllegalArgumentException("Categoria no valida: " + categoria);
    }
}
