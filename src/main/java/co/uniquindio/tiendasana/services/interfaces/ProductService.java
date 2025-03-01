package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.productDTO;
import jakarta.validation.Valid;

public interface ProductService {
    void createUpdateProduct(@Valid productDTO product);

    void deleteProduct(@Valid productDTO product);
}
