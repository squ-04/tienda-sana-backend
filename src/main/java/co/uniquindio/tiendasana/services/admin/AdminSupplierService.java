package co.uniquindio.tiendasana.services.admin;

import co.uniquindio.tiendasana.dto.admin.SupplierRequest;
import co.uniquindio.tiendasana.dto.admin.SupplierResponse;
import co.uniquindio.tiendasana.model.mongo.SupplierDocument;
import co.uniquindio.tiendasana.repos.mongo.SupplierDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSupplierService {

    private final SupplierDocumentRepository supplierRepo;

    public List<SupplierResponse> listAll() {
        return supplierRepo.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public SupplierResponse create(SupplierRequest req) {
        SupplierDocument d = SupplierDocument.builder()
                .category(req.category())
                .name(req.name())
                .product(req.product())
                .contact(req.contact())
                .address(req.address())
                .city(req.city())
                .active(true)
                .build();
        return toResponse(supplierRepo.save(d));
    }

    public SupplierResponse update(String id, SupplierRequest req) {
        SupplierDocument d = supplierRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + id));
        d.setCategory(req.category());
        d.setName(req.name());
        d.setProduct(req.product());
        d.setContact(req.contact());
        d.setAddress(req.address());
        d.setCity(req.city());
        return toResponse(supplierRepo.save(d));
    }

    public void deactivate(String id) {
        SupplierDocument d = supplierRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + id));
        d.setActive(false);
        supplierRepo.save(d);
    }

    private SupplierResponse toResponse(SupplierDocument d) {
        return new SupplierResponse(
                d.getId(),
                d.getCategory(),
                d.getName(),
                d.getProduct(),
                d.getContact(),
                d.getAddress(),
                d.getCity(),
                d.isActive()
        );
    }
}
