package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.GestorReservaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GestorReservaDocumentRepository extends MongoRepository<GestorReservaDocument, String> {

    Optional<GestorReservaDocument> findFirstByEmailUsuarioOrderByFechaDesc(String emailUsuario);

    long countByEmailUsuario(String emailUsuario);
}
