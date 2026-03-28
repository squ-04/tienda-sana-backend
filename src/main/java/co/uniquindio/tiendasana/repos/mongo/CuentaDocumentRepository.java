package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.CuentaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CuentaDocumentRepository extends MongoRepository<CuentaDocument, String> {

    Optional<CuentaDocument> findByDni(String dni);

    @Query("{ $or: [ { 'dni': ?0 }, { '_id': ?1 } ] }")
    List<CuentaDocument> findByDniOrEmailMatch(String dni, String email);
}
