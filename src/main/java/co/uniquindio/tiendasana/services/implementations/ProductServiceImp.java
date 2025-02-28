package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.repos.ProductRepo;
import co.uniquindio.tiendasana.services.interfaces.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProductServiceImp implements ProductService {
    private final ProductRepo productRepo;

    public ProductServiceImp(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }
}
