package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.productodtos.ListaProductos;
import co.uniquindio.tiendasana.dto.productodtos.ProductoInfoDTO;
import co.uniquindio.tiendasana.dto.productodtos.ProductoItemDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.Producto;

import java.io.IOException;
import java.util.List;

public interface ProductoService {

    /**
     * Metodo usado para obtener los detalles de un producto
     * @param id
     * @return
     */
    ProductoInfoDTO obtenerInfoProducto(String id);
    /**
     * Metodo usado para obtener lo informacion de los productos que los clientes
     * verán en primera instancia
     * @return Lista de items de productos
     * @throws IOException
     * @throws ProductoParseException
     */
    ListaProductos obtenerProductosCliente(int pagina) throws IOException, ProductoParseException;


    Producto getProducto(String id) throws ProductoParseException;
}
