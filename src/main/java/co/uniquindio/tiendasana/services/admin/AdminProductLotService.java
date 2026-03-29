package co.uniquindio.tiendasana.services.admin;

import co.uniquindio.tiendasana.dto.admin.InventoryResponse;
import co.uniquindio.tiendasana.dto.admin.ProductLotRequest;
import co.uniquindio.tiendasana.dto.admin.ProductLotResponse;
import co.uniquindio.tiendasana.model.mongo.ProductLotDocument;
import co.uniquindio.tiendasana.model.mongo.ProductoDocument;
import co.uniquindio.tiendasana.repos.mongo.ProductLotDocumentRepository;
import co.uniquindio.tiendasana.repos.mongo.ProductoDocumentRepository;
import co.uniquindio.tiendasana.repos.mongo.SupplierDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductLotService {

    private final ProductLotDocumentRepository lotRepo;
    private final ProductoDocumentRepository productRepo;
    private final SupplierDocumentRepository supplierRepo;

    public List<ProductLotResponse> listAll(String productIdFilter) {
        List<ProductLotDocument> list = (productIdFilter == null || productIdFilter.isBlank())
                ? lotRepo.findAllByOrderByEntryDateDesc()
                : lotRepo.findByProductIdOrderByEntryDateDesc(productIdFilter);
        return list.stream().map(this::toResponse).toList();
    }

    public ProductLotResponse create(ProductLotRequest req) {
        ProductoDocument product = productRepo.findById(req.productId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + req.productId()));
        supplierRepo.findById(req.supplierId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + req.supplierId()));

        ProductLotDocument lot = ProductLotDocument.builder()
                .productId(req.productId())
                .supplierId(req.supplierId())
                .entryDate(req.entryDate())
                .quantity(req.quantity())
                .unitValue(req.unitValue())
                .build();
        lot = lotRepo.save(lot);

        product.setStockQuantity(product.getStockQuantity() + req.quantity());
        productRepo.save(product);

        return toResponse(lot);
    }

    public ProductLotResponse update(String id, ProductLotRequest req) {
        ProductLotDocument lot = lotRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado: " + id));

        ProductoDocument product = productRepo.findById(lot.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + lot.getProductId()));

        int oldQty = lot.getQuantity();
        int newQty = req.quantity();
        int delta = newQty - oldQty;

        if (lot.getProductId().equals(req.productId())) {
            if (product.getStockQuantity() + delta < 0) {
                throw new IllegalArgumentException("El ajuste dejaría el stock del producto en negativo");
            }
        } else {
            if (product.getStockQuantity() < oldQty) {
                throw new IllegalArgumentException("Stock del producto origen inconsistente con la cantidad del lote");
            }
        }

        supplierRepo.findById(req.supplierId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + req.supplierId()));

        lot.setProductId(req.productId());
        lot.setSupplierId(req.supplierId());
        lot.setEntryDate(req.entryDate());
        lot.setQuantity(newQty);
        lot.setUnitValue(req.unitValue());

        if (!lot.getProductId().equals(product.getId())) {
            ProductoDocument newProduct = productRepo.findById(req.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + req.productId()));
            product.setStockQuantity(product.getStockQuantity() - oldQty);
            productRepo.save(product);
            newProduct.setStockQuantity(newProduct.getStockQuantity() + newQty);
            productRepo.save(newProduct);
        } else {
            product.setStockQuantity(product.getStockQuantity() + delta);
            productRepo.save(product);
        }

        return toResponse(lotRepo.save(lot));
    }

    /**
     * Elimina el lote y descuenta su cantidad del stock agregado del producto.
     */
    public void delete(String id) {
        ProductLotDocument lot = lotRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado: " + id));
        ProductoDocument product = productRepo.findById(lot.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + lot.getProductId()));
        int qty = lot.getQuantity();
        if (product.getStockQuantity() < qty) {
            throw new IllegalArgumentException(
                    "Stock del producto (" + product.getStockQuantity() + ") es menor que la cantidad del lote (" + qty + ")");
        }
        product.setStockQuantity(product.getStockQuantity() - qty);
        productRepo.save(product);
        lotRepo.deleteById(lot.getId());
    }

    public List<InventoryResponse> inventory() {
        return productRepo.findAllByOrderByNombreAsc().stream()
                .map(p -> new InventoryResponse(p.getId(), p.getNombre(), p.getStockQuantity()))
                .toList();
    }

    private ProductLotResponse toResponse(ProductLotDocument l) {
        return new ProductLotResponse(
                l.getId(),
                l.getProductId(),
                l.getSupplierId(),
                l.getEntryDate(),
                l.getQuantity(),
                l.getUnitValue()
        );
    }
}
