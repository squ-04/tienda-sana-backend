package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.ventadtos.CrearVentaProductoDTO;
import co.uniquindio.tiendasana.dto.ventadtos.PaymentResponseDTO;
import co.uniquindio.tiendasana.dto.ventadtos.VentaItemDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.VentaProducto;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface VentaProductoService {

    /**
     * Crear una venta de producto
     * @param crearVentaProductoDTO DTO con la información de la venta
     * @return ID de la venta creada
     * @throws Exception
     */
    String crearVenta(CrearVentaProductoDTO crearVentaProductoDTO) throws Exception;

    /**
     * Obtener una venta de producto por su ID
     * @param s ID de la venta
     * @return VentaProducto
     * @throws Exception
     */
    VentaProducto obtenerVentaProducto(String s) throws Exception;

    /**
     * Actualizar una venta de producto
     * @param idVentaProducto VentaProducto con la información actualizada
     * @return ID de la venta actualizada
     * @throws Exception
     */
    String borrarVentaProducto(String idVentaProducto) throws Exception;

    /**
     * Obtener información de una venta de producto
     * @param ventaProductoId ID de la venta
     * @return VentaItemDTO con la información de la venta
     * @throws Exception
     */
    VentaItemDTO obtenerInformacionVenta(String ventaProductoId) throws Exception;

    /**
     * Listar todas las ventas de un cliente
     * @param clienteId ID del cliente
     * @return Lista de VentaItemDTO con la información de las ventas
     * @throws IOException
     * @throws ProductoParseException
     */
    List<VentaItemDTO> listarVentasCliente(String clienteId) throws IOException, ProductoParseException;

    /**
     * Listar todas las ventas de un vendedor
     * @param ventaProductoId ID del vendedor
     * @return Lista de VentaItemDTO con la información de las ventas
     * @throws IOException
     * @throws ProductoParseException
     */
    PaymentResponseDTO makePayment(String ventaProductoId) throws Exception;

    /**
     * Enviar resumen de compra por correo electrónico
     * @return ID de la venta
     * @throws Exception
     */
    void receiveNotificationFromMercadoPago(Map<String, Object> request);

}
