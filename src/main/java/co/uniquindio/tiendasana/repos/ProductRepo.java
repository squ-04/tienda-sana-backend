package co.uniquindio.tiendasana.repos;


import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.enums.EstadoProducto;
import co.uniquindio.tiendasana.model.enums.CategoriaProducto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepo extends MongoRepository<Producto, String> {

    @Query("{'_id': ?0}")
    Optional<Producto> findById(String id);

    @Query("{'name': ?0}")
    Optional<Producto> findByName(String name);

    @Query("{'description': ?0}")
    List<Producto> findByDescription(String description);

    @Query("{'image': ?0}")
    List<Producto> findByImage(String imageUrl);

    @Query("{'productType': ?0}")
    List<Producto> findByProductType(CategoriaProducto type);

    @Query("{'status': ?0}")
    List<Producto> findByStatus(EstadoProducto status);

}
