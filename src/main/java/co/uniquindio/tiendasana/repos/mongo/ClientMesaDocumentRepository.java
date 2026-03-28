package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.ClientMesaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClientMesaDocumentRepository extends MongoRepository<ClientMesaDocument, String> {

    List<ClientMesaDocument> findByVisibleToClientTrueOrderByNombreAsc();

    long countByVisibleToClientTrue();
}
