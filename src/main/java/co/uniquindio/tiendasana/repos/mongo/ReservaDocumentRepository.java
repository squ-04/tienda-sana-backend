package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.ReservaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservaDocumentRepository extends MongoRepository<ReservaDocument, String> {
}
