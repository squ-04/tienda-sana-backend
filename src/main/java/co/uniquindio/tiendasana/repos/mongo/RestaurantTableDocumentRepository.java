package co.uniquindio.tiendasana.repos.mongo;

import co.uniquindio.tiendasana.model.mongo.RestaurantTableDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RestaurantTableDocumentRepository extends MongoRepository<RestaurantTableDocument, String> {

    List<RestaurantTableDocument> findAllByOrderByLocationAscCapacityAsc();
}
