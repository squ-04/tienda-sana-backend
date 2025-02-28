package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepo extends MongoRepository<Location, String> {
}
