package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.SupplierDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SupplierDocumentRepository extends MongoRepository<SupplierDocument, String> {

    List<SupplierDocument> findAllByOrderByNameAsc();
}
