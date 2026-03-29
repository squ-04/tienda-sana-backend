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
    private List<Mesa> mesas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaFinReserva;
    private float valorReserva;
    private int cantidadPersonas;
    private EstadoReserva estadoReserva;
    private String codigoPasarela;
    private Pago pago;


    @Builder
    public Reserva(String id, String usuarioId, List<Mesa> mesas, LocalDateTime fechaCreacion, LocalDateTime fechaReserva, LocalDateTime fechaFinReserva, float valorReserva, int cantidadPersonas, EstadoReserva estadoReserva, String codigoPasarela, Pago pago) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fechaCreacion = fechaCreacion;
        this.fechaReserva = fechaReserva;
        this.fechaFinReserva = fechaFinReserva;
        this.estadoReserva = estadoReserva;
        this.codigoPasarela = codigoPasarela;
        this.pago = pago;
        this.cantidadPersonas = cantidadPersonas;
        this.valorReserva = valorReserva;
        this.mesas = mesas;
    }





}
