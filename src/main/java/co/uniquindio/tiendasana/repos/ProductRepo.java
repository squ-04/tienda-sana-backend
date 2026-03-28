package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.dto.productodtos.ProductosTotalDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.mongo.ProductoDocument;
import co.uniquindio.tiendasana.repos.mongo.ProductoDocumentRepository;
import co.uniquindio.tiendasana.services.mongo.ProductCatalogMapper;
import co.uniquindio.tiendasana.utils.ProductoConstantes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class ProductRepo {

    private final ProductoDocumentRepository productMongo;
    private final ProductCatalogMapper catalogMapper;

    public ProductRepo(ProductoDocumentRepository productMongo, ProductCatalogMapper catalogMapper) {
        this.productMongo = productMongo;
        this.catalogMapper = catalogMapper;
    }

    public ProductosTotalDTO obtenerProductos(int pagina, int cantidadElementos) throws IOException, ProductoParseException {
        var page = productMongo.findByActiveTrueAndOutOfStockFalseAndStockQuantityGreaterThan(
                0,
                PageRequest.of(pagina, cantidadElementos, Sort.by("nombre")));
        List<Producto> productos = page.getContent().stream()
                .map(catalogMapper::toLegacy)
                .collect(Collectors.toList());
        return new ProductosTotalDTO((int) page.getTotalElements(), productos);
    }

    public List<Producto> obtenerProductos(String hoja) throws IOException, ProductoParseException {
        List<ProductoDocument> docs;
        if (ProductoConstantes.HOJACLIENTE.equals(hoja)) {
            docs = productMongo.findAllByActiveTrueAndOutOfStockFalseAndStockQuantityGreaterThanOrderByNombreAsc(0);
        } else {
            docs = productMongo.findAll(Sort.by("nombre"));
        }
        return docs.stream().map(catalogMapper::toLegacy).collect(Collectors.toList());
    }

    public int contarProductosExistintes() {
        return (int) productMongo.count();
    }

    public Producto mapearProducto(List<Object> row) {
        String nombre = row.get(0).toString();
        String descripcion = row.get(1).toString();
        String categoria = row.get(2).toString();
        String estado = row.get(3).toString();
        int cantidad = Integer.parseInt(row.get(4).toString());
        String imagen = row.get(5).toString();
        float precioUnitario = Float.parseFloat(row.get(6).toString());
        String id = row.get(7).toString();
        return Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .categoria(categoria)
                .estado(estado)
                .cantidad(cantidad)
                .imagen(imagen)
                .precioUnitario(precioUnitario)
                .id(id)
                .build();
    }

    public List<Object> mapearProductoInverso(Producto producto) {
        return Arrays.asList(
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria(),
                producto.getEstado(),
                producto.getCantidad(),
                producto.getImagen(),
                ((int) producto.getPrecioUnitario()),
                producto.getId()
        );
    }

    public List<Producto> filtrar(Predicate<Producto> expresion, String hoja) throws IOException, ProductoParseException {
        return obtenerProductos(hoja).stream().filter(expresion).collect(Collectors.toList());
    }

    public int obtenerIndiceProducto(String id, String hoja) {
        List<Producto> productos;
        try {
            productos = obtenerProductos(hoja);
        } catch (IOException | ProductoParseException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public void actualizar(Producto producto) throws IOException {
        ProductoDocument doc = productMongo.findById(producto.getId())
                .orElseThrow(() -> new IOException("Registro no encontrado"));
        catalogMapper.applyLegacyToDocument(producto, doc);
        productMongo.save(doc);
    }

    public Optional<Producto> obtenerPorId(String id) throws IOException, ProductoParseException {
        Optional<ProductoDocument> opt = productMongo.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        ProductoDocument d = opt.get();
        if (!d.isActive() || d.isOutOfStock() || d.getStockQuantity() <= 0) {
            return Optional.empty();
        }
        return Optional.of(catalogMapper.toLegacy(d));
    }
}
