package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.VentaProductoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VentaProductoDocumentRepository extends MongoRepository<VentaProductoDocument, String> {
}
