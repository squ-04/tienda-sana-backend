package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepo extends MongoRepository<Mesa, String> {

    @Query("{'_id': ?0}")
    Optional<Mesa> findById(String id);

    @Query("{'name': ?0}")
    Optional<Mesa> findByName(String name);

    @Query("{'status': ?0}")
    List<Mesa> findByStatus(EstadoMesa status);

    @Query("{'locationId': ?0}")
    List<Mesa> findByLocationId(String locationId);

    @Query("{'name': ?0,locationId: ?1}")
    List<Mesa> findByNameAndLocationId(String name, String locationId);

}
