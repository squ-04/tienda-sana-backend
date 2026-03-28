package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.CarritoComprasDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CarritoComprasDocumentRepository extends MongoRepository<CarritoComprasDocument, String> {

    Optional<CarritoComprasDocument> findByIdUsuario(String idUsuario);
}
