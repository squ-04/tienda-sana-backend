package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Location;
import co.uniquindio.tiendasana.model.documents.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepo extends MongoRepository<Location, String> {

    @Query("{'_id': ?0}")
    Optional<Location> findById(String id);

    @Query("{'name': ?0}")
    Optional<Location> findByName(String name);

    @Query("{'description': ?0}")
    List<Location> findByDescription(String description);

}
