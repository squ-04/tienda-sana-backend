package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.TableDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TableDocumentRepository extends MongoRepository<TableDocument, String> {

    List<TableDocument> findByVisibleToClientTrueOrderByNombreAsc();

    long countByVisibleToClientTrue();
}
