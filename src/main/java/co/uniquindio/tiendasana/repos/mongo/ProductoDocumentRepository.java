package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.ProductoDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductoDocumentRepository extends MongoRepository<ProductoDocument, String> {

    Page<ProductoDocument> findByActiveTrueAndOutOfStockFalseAndStockQuantityGreaterThan(
            int minExclusive, Pageable pageable);

    long countByActiveTrueAndOutOfStockFalseAndStockQuantityGreaterThan(int minExclusive);

    List<ProductoDocument> findAllByActiveTrueAndOutOfStockFalseAndStockQuantityGreaterThanOrderByNombreAsc(
            int minExclusive);

    List<ProductoDocument> findAllByOrderByNombreAsc();
}
