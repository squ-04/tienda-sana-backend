package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.productDTO;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.repos.ProductRepo;
import co.uniquindio.tiendasana.services.interfaces.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProductServiceImp implements ProductService {
    private final ProductRepo productRepo;

    public ProductServiceImp(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    public void createUpdateProduct(productDTO productInfo) {
        Optional<Producto> productObtained=productRepo.findByName(productInfo.name());
        Producto product=new Producto(
             //TODO llenar producto
        );
        productObtained.ifPresent(producto -> product.setId(producto.getId()));
        productRepo.save(product);
    }

    @Override
    public void deleteProduct(productDTO productInfo) {
        Optional<Producto> productObtained=productRepo.findByName(productInfo.name());
        productRepo.delete(productObtained.get());
    }


}
