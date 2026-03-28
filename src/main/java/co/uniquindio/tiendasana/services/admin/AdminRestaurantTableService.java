package co.uniquindio.tiendasana.services.admin;

import co.uniquindio.tiendasana.dto.admin.RestaurantTableRequest;
import co.uniquindio.tiendasana.dto.admin.RestaurantTableResponse;
import co.uniquindio.tiendasana.dto.admin.TableStatusPatchRequest;
import co.uniquindio.tiendasana.model.enums.TableStatus;
import co.uniquindio.tiendasana.model.mongo.RestaurantTableDocument;
import co.uniquindio.tiendasana.repos.mongo.RestaurantTableDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRestaurantTableService {

    private final RestaurantTableDocumentRepository tableRepo;

    public List<RestaurantTableResponse> listAll() {
        return tableRepo.findAllByOrderByLocationAscCapacityAsc().stream().map(this::toResponse).toList();
    }

    public RestaurantTableResponse create(RestaurantTableRequest req) {
        RestaurantTableDocument d = RestaurantTableDocument.builder()
                .capacity(req.capacity())
                .location(req.location())
                .active(req.active())
                .status(TableStatus.AVAILABLE)
                .build();
        return toResponse(tableRepo.save(d));
    }

    public RestaurantTableResponse update(String id, RestaurantTableRequest req) {
        RestaurantTableDocument d = tableRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada: " + id));
        d.setCapacity(req.capacity());
        d.setLocation(req.location());
        d.setActive(req.active());
        return toResponse(tableRepo.save(d));
    }

    public RestaurantTableResponse patchStatus(String id, TableStatusPatchRequest req) {
        RestaurantTableDocument d = tableRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada: " + id));
        d.setStatus(req.status());
        return toResponse(tableRepo.save(d));
    }

    private RestaurantTableResponse toResponse(RestaurantTableDocument d) {
        return new RestaurantTableResponse(
                d.getId(),
                d.getCapacity(),
                d.getLocation(),
                d.isActive(),
                d.getStatus()
        );
    }
}
