package co.uniquindio.tiendasana.services.admin;

import co.uniquindio.tiendasana.dto.admin.RestaurantTableRequest;
import co.uniquindio.tiendasana.dto.admin.RestaurantTableResponse;
import co.uniquindio.tiendasana.dto.admin.TableStatusPatchRequest;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.Localidad;
import co.uniquindio.tiendasana.model.enums.TableStatus;
import co.uniquindio.tiendasana.model.mongo.TableDocument;
import co.uniquindio.tiendasana.repos.mongo.TableDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminRestaurantTableService {

    private static final List<Integer> DURACIONES_PERMITIDAS = List.of(30, 60, 90, 120);

    private final TableDocumentRepository tableRepo;

    public List<RestaurantTableResponse> listAll() {
        return tableRepo.findAll().stream()
                .sorted(Comparator
                        .comparing((TableDocument d) -> d.getLocalidad() == null ? "" : d.getLocalidad(),
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(d -> d.getNombre() == null ? "" : d.getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    public RestaurantTableResponse create(RestaurantTableRequest req) {
        validateEstado(req.estado());
        validateLocalidad(req.localidad());
        validateDuracionReserva(req.duracionReservaMinutos());
        String id = "mesa-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        TableDocument d = TableDocument.builder()
                .id(id)
                .nombre(req.nombre().trim())
                .estado(normalizeEstado(req.estado()))
                .localidad(normalizeLocalidad(req.localidad()))
                .precioReserva(req.precioReserva())
                .capacidad(req.capacidad())
                .duracionReservaMinutos(req.duracionReservaMinutos())
                .imagen(req.imagen().trim())
                .visibleToClient(req.visibleToClient())
                .build();
        return toResponse(tableRepo.save(d));
    }

    public RestaurantTableResponse update(String id, RestaurantTableRequest req) {
        validateEstado(req.estado());
        validateLocalidad(req.localidad());
        validateDuracionReserva(req.duracionReservaMinutos());
        TableDocument d = tableRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada: " + id));
        d.setNombre(req.nombre().trim());
        d.setEstado(normalizeEstado(req.estado()));
        d.setLocalidad(normalizeLocalidad(req.localidad()));
        d.setPrecioReserva(req.precioReserva());
        d.setCapacidad(req.capacidad());
        d.setDuracionReservaMinutos(req.duracionReservaMinutos());
        d.setImagen(req.imagen().trim());
        d.setVisibleToClient(req.visibleToClient());
        return toResponse(tableRepo.save(d));
    }

    public RestaurantTableResponse patchStatus(String id, TableStatusPatchRequest req) {
        TableDocument d = tableRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada: " + id));
        d.setEstado(toEstadoCliente(req.status()));
        return toResponse(tableRepo.save(d));
    }

    private void validateEstado(String estado) {
        EstadoMesa.fromEstado(estado.trim());
    }

    private void validateLocalidad(String localidad) {
        Localidad.fromLocalidad(localidad.trim());
    }

    private String normalizeEstado(String estado) {
        return EstadoMesa.fromEstado(estado.trim()).getEstado();
    }

    private String normalizeLocalidad(String localidad) {
        return Localidad.fromLocalidad(localidad.trim()).getLocalidad();
    }

    private void validateDuracionReserva(Integer duracionReservaMinutos) {
        if (duracionReservaMinutos == null || !DURACIONES_PERMITIDAS.contains(duracionReservaMinutos)) {
            throw new IllegalArgumentException("La duración de reserva debe ser una de estas opciones: 30, 60, 90 o 120 minutos");
        }
    }

    private String toEstadoCliente(TableStatus s) {
        return switch (s) {
            case RESERVED -> EstadoMesa.RESERVADA.getEstado();
            case OCCUPIED -> EstadoMesa.OCUPADA.getEstado();
            case AVAILABLE -> EstadoMesa.DISPONIBLE.getEstado();
        };
    }

    private RestaurantTableResponse toResponse(TableDocument d) {
        return new RestaurantTableResponse(
                d.getId(),
                d.getNombre(),
                d.getEstado(),
                d.getLocalidad(),
                d.getPrecioReserva(),
                d.getCapacidad(),
                normalizeDuracionReserva(d.getDuracionReservaMinutos()),
                d.getImagen(),
                d.isVisibleToClient()
        );
    }

    private int normalizeDuracionReserva(Integer duracionReservaMinutos) {
        if (duracionReservaMinutos == null || duracionReservaMinutos <= 0) {
            return TableDocument.DEFAULT_DURACION_RESERVA_MINUTOS;
        }
        return duracionReservaMinutos;
    }
}
