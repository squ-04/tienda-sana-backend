package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Table;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableRepo extends MongoRepository<Table, String> {
}
