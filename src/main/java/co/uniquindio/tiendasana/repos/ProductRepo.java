package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.Product;
import co.uniquindio.tiendasana.model.enums.ProductStatus;
import co.uniquindio.tiendasana.model.enums.ProductType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepo extends MongoRepository<Product, String> {

    @Query("{'_id': ?0}")
    Optional<Product> findById(String id);

    @Query("{'name': ?0}")
    Optional<Product> findByName(String name);

    @Query("{'description': ?0}")
    List<Product> findByDescription(String description);

    @Query("{'image': ?0}")
    List<Product> findByImage(String imageUrl);

    @Query("{'productType': ?0}")
    List<Product> findByProductType(ProductType type);

    @Query("{'status': ?0}")
    List<Product> findByStatus(ProductStatus status);

}
