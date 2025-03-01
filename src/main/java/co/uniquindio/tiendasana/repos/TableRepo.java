package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Table;
import co.uniquindio.tiendasana.model.enums.TableStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepo extends MongoRepository<Table, String> {

    @Query("{'_id': ?0}")
    Optional<Table> findById(String id);

    @Query("{'name': ?0}")
    Optional<Table> findByName(String name);

    @Query("{'status': ?0}")
    List<Table> findByStatus(TableStatus status);

    @Query("{'locationId': ?0}")
    List<Table> findByLocationId(String locationId);

    @Query("{'name': ?0,locationId: ?1}")
    List<Table> findByNameAndLocationId(String name, String locationId);

}
