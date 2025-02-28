package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepo extends MongoRepository<Product, String> {
}
