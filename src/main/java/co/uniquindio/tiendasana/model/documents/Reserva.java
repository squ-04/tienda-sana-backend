package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.EstadoReserva;
import co.uniquindio.tiendasana.model.vo.Pago;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reserva {
    @EqualsAndHashCode.Include
    private String id;

    private String usuarioId;
    private LocalDateTime fechaReserva;
    private EstadoReserva estadoReserva;
    private String codigoPasarela;
    private Pago pago;
    private int cantidadPersonas;
    private float valorReserva;
    private List<Mesa> mesas;

    @Builder
    public Reserva(String usuarioId, LocalDateTime fechaReserva, EstadoReserva estadoReserva, String codigoPasarela, Pago pago, int cantidadPersonas, float valorReserva, List<Mesa> mesas) {
        this.usuarioId = usuarioId;
        this.fechaReserva = fechaReserva;
        this.estadoReserva = estadoReserva;
        this.codigoPasarela = codigoPasarela;
        this.pago = pago;
        this.cantidadPersonas = cantidadPersonas;
        this.valorReserva = valorReserva;
        this.mesas = mesas;
    }





}
