package co.uniquindio.tiendasana.services.admin;

import co.uniquindio.tiendasana.dto.admin.RestaurantTableRequest;
import co.uniquindio.tiendasana.dto.admin.RestaurantTableResponse;
import co.uniquindio.tiendasana.dto.admin.TableStatusPatchRequest;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
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
        String id = "mesa-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        TableDocument d = TableDocument.builder()
                .id(id)
                .nombre(req.nombre().trim())
                .estado(normalizeEstado(req.estado()))
                .localidad(req.localidad().trim())
                .precioReserva(req.precioReserva())
                .capacidad(req.capacidad())
                .imagen(req.imagen().trim())
                .visibleToClient(req.visibleToClient())
                .build();
        return toResponse(tableRepo.save(d));
    }

    public RestaurantTableResponse update(String id, RestaurantTableRequest req) {
        validateEstado(req.estado());
        TableDocument d = tableRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada: " + id));
        d.setNombre(req.nombre().trim());
        d.setEstado(normalizeEstado(req.estado()));
        d.setLocalidad(req.localidad().trim());
        d.setPrecioReserva(req.precioReserva());
        d.setCapacidad(req.capacidad());
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

    private String normalizeEstado(String estado) {
        return EstadoMesa.fromEstado(estado.trim()).getEstado();
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
                d.getImagen(),
                d.isVisibleToClient()
        );
    }
}
