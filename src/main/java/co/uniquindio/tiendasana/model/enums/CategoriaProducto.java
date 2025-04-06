package co.uniquindio.tiendasana.model.enums;

import lombok.Getter;

@Getter
public enum CategoriaProducto {

    BEBIDA ("Bebida"),
    CEREALES("Cereales"),
    FRUTAS ("Frutas"),
    FRUTOSSECOS("Frutos secos"),
    LACTEOS ("Lacteos"),
    PANADERIA ("Panadería"),
    TE("Té");


    private final String categoria;


     CategoriaProducto(String categoria) {
         this.categoria = categoria;
     }

    public static CategoriaProducto fromCategoria(String categoria) {
        for (CategoriaProducto categoriaProducto : CategoriaProducto.values()) {
            if (categoriaProducto.getCategoria().equalsIgnoreCase(categoria)) {
                return categoriaProducto;
            }
        }
        throw new IllegalArgumentException("Categoria no valida: " + categoria);
    }
}
