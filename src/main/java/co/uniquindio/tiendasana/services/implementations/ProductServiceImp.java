package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.productDTO;
import co.uniquindio.tiendasana.model.documents.Product;
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
        Optional<Product> productObtained=productRepo.findByName(productInfo.name());
        Product product=new Product(
                productInfo.name(),
                productInfo.description(),
                productInfo.image(),
                productInfo.productType(),
                productInfo.status()
        );
        if (productObtained.isPresent()) {
            product.setId(productObtained.get().getId());
        }
        productRepo.save(product);
    }

    @Override
    public void deleteProduct(productDTO productInfo) {
        Optional<Product> productObtained=productRepo.findByName(productInfo.name());
        productRepo.delete(productObtained.get());
    }


}
