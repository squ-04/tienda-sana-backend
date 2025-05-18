package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.carritoComprasdtos.AgregarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.BorrarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.EditarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.VistaItemCarritoDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

public interface CarritoComprasService {


    /**
     * Elimina todos los productos del carrito de compras asociado al usuario dado.
     * @param emailAutenticado Email del usuario cuyo carrito se va a limpiar.
     * @throws IOException Si ocurre un error al acceder al repositorio.
     */
    void borrarTodosLosItemsDelCarrito(String emailAutenticado) throws IOException;


    /**
     * Obtiene el carrito de compras del usuario si existe.
     * @param idUsuario Email del usuario.
     * @return El carrito de compras correspondiente.
     * @throws Exception Si el carrito no existe.
     */
    CarritoCompras getCarritoCompras(String idUsuario, String emailAutenticado) throws Exception;

    /**
     * Agrega un producto al carrito de compras del usuario.
     * Si el carrito no existe, se crea uno nuevo.
     * @param addShoppingCarDetailDTO DTO con la información del producto a agregar.
     * @return El ID del carrito actualizado.
     * @throws IOException            Si hay un error al guardar.
     * @throws ProductoParseException Si ocurre un error relacionado con el producto.
     */
    String agregarDetalleCarrito(AgregarDetalleCarritoDTO addShoppingCarDetailDTO, String emailAutenticado) throws IOException, ProductoParseException;

    /**
     * Modifica la cantidad de un producto en el carrito de compras.
     * Valida la disponibilidad del stock antes de aplicar los cambios.
     * @param editCarDetailDTO DTO con la nueva cantidad y producto.
     * @return El ID del carrito actualizado.
     * @throws Exception Si el producto no tiene stock o el carrito no existe.
     */
    String editarDetalleCarrito(EditarDetalleCarritoDTO editarCarritoDetalleDTO, String emailAutenticado) throws Exception;
    /**
     * Elimina un producto específico del carrito de compras del usuario.
     * @param deleteCarDetailDTO DTO con la información del producto y usuario.
     * @return ID del carrito actualizado.
     * @throws Exception Si el carrito no existe.
     */
    String borrarItemDelCarrito(BorrarDetalleCarritoDTO borrarDetalleCarritoDTO, String emailAutenticado) throws Exception;

    /**
     * Lista todos los productos dentro del carrito de compras del usuario.
     * @param emailUsuario Correo electrónico o ID del usuario.
     * @return Lista de DTOs representando los ítems del carrito.
     * @throws IOException Si hay un error al acceder a los datos.
     */
    List<VistaItemCarritoDTO> listarDetallesCarrito(String emailUsuario, String emailAutenticado) throws IOException;

    /**
     * Crea un nuevo carrito de compras para el usuario si aún no tiene uno.
     * @param idUsuario ID del usuario.
     * @return El carrito de compras existente o recién creado.
     * @throws IOException Si ocurre un error al guardar.
     */
    CarritoCompras crearCarritoCompras(String idUsuario) throws IOException;

}
