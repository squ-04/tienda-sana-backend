package co.uniquindio.tiendasana.model.enums;

import lombok.Getter;

@Getter
public enum EstadoReserva {
    PENDIENTE ("Pendiente"),
    CONFIRMADA ("Confirmada"),
    CANCELADA ("Cancelada");

    private final String estadoReserva;

    EstadoReserva(String estadoReserva) {
        this.estadoReserva = estadoReserva;
    }


    // Método para convertir un String en Estado de reserva
    public static EstadoReserva fromEstadoReserva(String estado) {
        for (EstadoReserva estadoR : EstadoReserva.values()) {
            if (estadoR.getEstadoReserva().equalsIgnoreCase(estado)) {
                return estadoR;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + estado);
    }
}
