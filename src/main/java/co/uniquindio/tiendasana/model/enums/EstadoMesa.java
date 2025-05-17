package co.uniquindio.tiendasana.model.enums;

import lombok.Getter;

@Getter
public enum EstadoMesa {
    //Status for the tables
    DISPONIBLE ("Disponible"),
    INHABILITADA ("Inhabilitada"),
    OCUPADA ("Ocupada");


    private final String estado;


    EstadoMesa (String estado) {
        this.estado = estado;
    }


    public static EstadoMesa fromEstado(String estado) {
        for (EstadoMesa estadoMesa : EstadoMesa.values()) {
            if (estadoMesa.getEstado().equalsIgnoreCase(estado)) {
                return estadoMesa;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + estado);
    }
}
