package co.uniquindio.tiendasana.services.mongo;

import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.enums.CategoriaProducto;
import co.uniquindio.tiendasana.model.enums.EstadoProducto;
import co.uniquindio.tiendasana.model.mongo.ProductoDocument;
import org.springframework.stereotype.Component;

/**
 * Convierte entre {@link ProductoDocument} (Mongo) y la entidad legacy {@link Producto}
 * usada por carrito, ventas y DTOs públicos.
 */
@Component
public class ProductCatalogMapper {

    public Producto toLegacy(ProductoDocument doc) {
        Producto p = new Producto();
        p.setId(doc.getId());
        p.setNombre(doc.getNombre());
        p.setDescripcion(doc.getDescripcion());
        p.setCategoriaLibre(doc.getCategoria());
        try {
            p.setCategoria(CategoriaProducto.fromCategoria(doc.getCategoria()));
        } catch (IllegalArgumentException ex) {
            p.setCategoria(CategoriaProducto.OTROS);
        }
        boolean vendible = doc.isActive() && !doc.isOutOfStock() && doc.getStockQuantity() > 0;
        String estadoTxt = vendible ? EstadoProducto.DISPONIBLE.getEstado() : EstadoProducto.AGOTADO.getEstado();
        p.setEstadoLibre(estadoTxt);
        p.setEstado(vendible ? EstadoProducto.DISPONIBLE : EstadoProducto.AGOTADO);
        p.setCantidad(doc.getStockQuantity());
        p.setImagen(doc.getImagen());
        p.setPrecioUnitario((float) doc.getPrecioUnitario());
        p.setCalificacionPromedio(doc.getCalificacionPromedio());
        return p;
    }

    /**
     * Actualiza un documento existente con campos del modelo legacy {@link Producto}.
     */
    public void applyLegacyToDocument(Producto legacy, ProductoDocument doc) {
        doc.setNombre(legacy.getNombre());
        doc.setDescripcion(legacy.getDescripcion());
        doc.setCategoria(legacy.getCategoria());
        doc.setImagen(legacy.getImagen());
        doc.setPrecioUnitario(legacy.getPrecioUnitario());
        doc.setStockQuantity(legacy.getCantidad());
        doc.setCalificacionPromedio(legacy.getCalificacionPromedio());
        boolean vendible = legacy.getEstado() != null
                && legacy.getEstado().equalsIgnoreCase(EstadoProducto.DISPONIBLE.getEstado());
        doc.setOutOfStock(!vendible);
        doc.setActive(true);
    }
}
