package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.documents.Reserva;
import co.uniquindio.tiendasana.model.enums.EstadoReserva;
import co.uniquindio.tiendasana.model.mongo.ReservaDocument;
import co.uniquindio.tiendasana.model.vo.Pago;
import co.uniquindio.tiendasana.repos.mongo.ReservaDocumentRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class ReservasRepo {

    private final ReservaDocumentRepository mongo;

    public ReservasRepo(ReservaDocumentRepository mongo) {
        this.mongo = mongo;
    }

    public Reserva guardarReserva(Reserva reserva) throws IOException {
        if (reserva.getId() == null || reserva.getId().isEmpty() || reserva.getId().equals("-")) {
            reserva.setId(UUID.randomUUID().toString());
        }
        if (reserva.getMesas() != null) {
            for (Mesa mesa : reserva.getMesas()) {
                if (mesa != null) {
                    mesa.setIdReserva(reserva.getId());
                }
            }
        }
        mongo.save(toDocument(reserva));
        return reserva;
    }

    public List<Reserva> filtrarReservasSimple(Predicate<Reserva> expresion) throws IOException {
        return mongo.findAll().stream()
                .map(this::toReserva)
                .filter(expresion)
                .collect(Collectors.toList());
    }

    public void actualizarReservaSimple(Reserva reserva) throws IOException {
        if (reserva.getId() == null || !mongo.existsById(reserva.getId())) {
            System.err.println("No se pudo actualizar la reserva simple. Registro no encontrado para ID: "
                    + (reserva != null ? reserva.getId() : null));
            return;
        }
        mongo.save(toDocument(reserva));
    }

    private ReservaDocument toDocument(Reserva r) {
        String estadoStr = r.getEstadoReserva() != null ? r.getEstadoReserva().getEstadoReserva() : EstadoReserva.PENDIENTE.getEstadoReserva();
        List<Mesa> mesas = r.getMesas() != null ? new ArrayList<>(r.getMesas()) : new ArrayList<>();
        return ReservaDocument.builder()
                .id(r.getId())
                .usuarioId(r.getUsuarioId())
                .mesas(mesas)
                .fechaReserva(r.getFechaReserva())
                .valorReserva(r.getValorReserva())
                .cantidadPersonas(r.getCantidadPersonas())
                .estadoReserva(estadoStr)
                .codigoPasarela(r.getCodigoPasarela())
                .pago(r.getPago())
                .build();
    }

    private Reserva toReserva(ReservaDocument d) {
        EstadoReserva estado = EstadoReserva.PENDIENTE;
        try {
            estado = EstadoReserva.fromEstadoReserva(d.getEstadoReserva());
        } catch (IllegalArgumentException ignored) {
            // mantener PENDIENTE
        }
        return Reserva.builder()
                .id(d.getId())
                .usuarioId(d.getUsuarioId())
                .mesas(d.getMesas() != null ? new ArrayList<>(d.getMesas()) : new ArrayList<>())
                .fechaReserva(d.getFechaReserva())
                .valorReserva((float) d.getValorReserva())
                .cantidadPersonas(d.getCantidadPersonas())
                .estadoReserva(estado)
                .codigoPasarela(d.getCodigoPasarela())
                .pago(d.getPago())
                .build();
    }
}
