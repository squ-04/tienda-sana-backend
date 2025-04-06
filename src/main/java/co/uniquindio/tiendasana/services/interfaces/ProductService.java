package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.productDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoInfoDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoItemDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.Producto;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    /**
     * Metodo usado para obtener los detalles de un producto
     * @param id
     * @return
     */
    ProductoInfoDTO obtenerInfoProducto(String id);

    List<ProductoItemDTO> obtenerProductosCliente() throws IOException, ProductoParseException;
}
