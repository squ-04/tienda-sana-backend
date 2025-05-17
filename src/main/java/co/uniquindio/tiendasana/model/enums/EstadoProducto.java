package co.uniquindio.tiendasana.model.enums;

import lombok.Getter;


@Getter
public enum EstadoProducto {
    AGOTADO ("Agotado"),
    DISPONIBLE("Disponible");

    private final String estado;

     EstadoProducto(String estado) {
        this.estado = estado;
    }


    // Método para convertir un String en Estado de producto
    public static EstadoProducto fromEstado(String estado) {
        for (EstadoProducto estadoProducto : EstadoProducto.values()) {
            if (estadoProducto.getEstado().equalsIgnoreCase(estado)) {
                return estadoProducto;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + estado);
    }
}
