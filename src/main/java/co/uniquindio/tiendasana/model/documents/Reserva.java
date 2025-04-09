package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.EstadoReserva;
import co.uniquindio.tiendasana.model.vo.Pago;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reserva {
    @EqualsAndHashCode.Include
    private String id;

    private String usuarioId;
    private String mesaId;
    private LocalDateTime fechaReserva;
    private EstadoReserva estadoReserva;
    private String codigoPasarela;
    private Pago pago;
    private int cantidadPersonas;
    private float valorReserva;

    @Builder
    public Reserva(String usuarioId, String mesaId, LocalDateTime fechaReserva, EstadoReserva estadoReserva, String codigoPasarela, Pago pago, int cantidadPersonas, float valorReserva) {
        this.usuarioId = usuarioId;
        this.mesaId = mesaId;
        this.fechaReserva = fechaReserva;
        this.estadoReserva = estadoReserva;
        this.codigoPasarela = codigoPasarela;
        this.pago = pago;
        this.cantidadPersonas = cantidadPersonas;
        this.valorReserva = valorReserva;
    }





}
