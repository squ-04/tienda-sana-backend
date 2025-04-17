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

    String crearVenta(CrearVentaProductoDTO crearVentaProductoDTO) throws Exception;

    VentaProducto obtenerVentaProducto(String s) throws ResourceNotFoundException, IOException, ProductoParseException;

    String borrarVentaProducto(String idVentaProducto) throws Exception;

    VentaItemDTO obtenerInformacionVenta(String ventaProductoId) throws ResourceNotFoundException, IOException, ProductoParseException;

    List<VentaItemDTO> listarVentasCliente(String clienteId) throws IOException, ProductoParseException;

    PaymentResponseDTO makePayment(String ventaProductoId) throws Exception;

    void receiveNotificationFromMercadoPago(Map<String, Object> request);

}
