package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.MesaDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.AgregarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.BorrarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.EditarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.VistaItemCarritoDTO;
import co.uniquindio.tiendasana.dto.gestorReservasdtos.BorrarMesaGestorDTO;
import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.dto.reservadtos.CrearReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.PaymentResponseReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.ReservaItemDTO;
import co.uniquindio.tiendasana.dto.ventadtos.CrearVentaProductoDTO;
import co.uniquindio.tiendasana.dto.ventadtos.PaymentResponseDTO;
import co.uniquindio.tiendasana.dto.ventadtos.VentaItemDTO;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.services.interfaces.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cliente")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final CarritoComprasService carritoComprasService;
    private final ProductoService productoService;
    private final VentaProductoService ventaProductoService;
    private final PromocionService promocionService;
    private final ReservaService reservaService;
    private final GestorReservasService gestorReservasService;

    /**
     * Controlador para agregar un producto al carrito de compras
     * @param addShoppingCarDetailDTO Detalle del producto a agregar al carrito
     * @return ResponseEntity con el id del carrito de compras
     * @throws Exception
     */
    @PutMapping("/carrito/add-item")
    public ResponseEntity<MessageDTO<String>> agregarDetalleCarrito
            (@Valid @RequestBody AgregarDetalleCarritoDTO addShoppingCarDetailDTO) throws Exception{
        String shoppingCarId;
        shoppingCarId= carritoComprasService.agregarDetalleCarrito(addShoppingCarDetailDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, shoppingCarId));
    }

    @PutMapping("/gestor-reservas/add-item")
    public ResponseEntity<MessageDTO<String>> agregarMesaGestorReservas
            (@Valid @RequestBody MesaDTO mesaDTO) throws Exception{
        String gestorReservaId = gestorReservasService.agregarMesaGestorReservas(mesaDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, gestorReservaId));
    }



    /**
     * Controlador para editar un producto en el carrito de compras
     * @param editarDetalleCarritoDTO Detalle del producto a editar en el carrito
     * @return ResponseEntity con el id del carrito de compras
     * @throws Exception
     */
    @PutMapping("/carrito/edit-item")
    public ResponseEntity<MessageDTO<String>> editarDetalleCarrito (@Valid @RequestBody EditarDetalleCarritoDTO editarDetalleCarritoDTO) throws Exception{
        String shoppingCarId= carritoComprasService.editarDetalleCarrito(editarDetalleCarritoDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, shoppingCarId));
    }

    /**
     * Controlador para eliminar un producto del carrito de compras
     * @param deleteCarDetailDTO Detalle del producto a eliminar del carrito
     * @return ResponseEntity con el id del carrito de compras
     * @throws Exception
     */
    @DeleteMapping("/carrito/delete-item")
    public ResponseEntity<MessageDTO<String>> borrarDetalleCarrito(@Valid @RequestBody BorrarDetalleCarritoDTO deleteCarDetailDTO) throws Exception{
        String shoppingCarId= carritoComprasService.borrarCarritoCompras(deleteCarDetailDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, shoppingCarId));
    }

    @DeleteMapping("/gestor-reservas/delete-item")
    public ResponseEntity<MessageDTO<String>> borrarMesaGestorReservas(@Valid @RequestBody BorrarMesaGestorDTO mesaBorrarDTO) throws Exception{
        String gestorReservasId= gestorReservasService.borrarMesaGestorReservas(mesaBorrarDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, gestorReservasId));
    }

    /**
     * Controlador para listar los productos del carrito de compras
     * @param emailUsuario Email del usuario
     * @return ResponseEntity con la lista de productos del carrito de compras
     * @throws Exception
     */
    @GetMapping("/carrito/get-items/{emailUsuario}")
    public ResponseEntity<MessageDTO<List<VistaItemCarritoDTO>>> listarDetallesCarrito(@PathVariable String emailUsuario) throws Exception{
        List<VistaItemCarritoDTO> carItems = carritoComprasService.listarDetallesCarrito(emailUsuario);
        return ResponseEntity.ok(new MessageDTO<>(false, carItems));
    }


    @GetMapping("/gestor-reservas/get-items/{emailUsuario}")
    public ResponseEntity<MessageDTO<List<MesaDTO>>> listarMesasGestorReservas(@PathVariable String emailUsuario) throws Exception{
        List<MesaDTO> gestorItems = gestorReservasService.obtenerMesasGestorReservas(emailUsuario);
        return ResponseEntity.ok(new MessageDTO<>(false, gestorItems));
    }

    /**
     * Controlador para hacer el pago de uno o varios productos
     * @param ventaProductoId Id de la venta
     * @return ResponseEntity con el id de la venta
     * @throws Exception
     */
    @PostMapping("/venta/make-payment/{ventaProductoId}")
    public ResponseEntity<MessageDTO<PaymentResponseDTO>> makePayment(@PathVariable String ventaProductoId) throws Exception {
        // Llamamos al método del servicio para crear el

        PaymentResponseDTO paymentResponse = ventaProductoService.makePayment(ventaProductoId);
        return ResponseEntity.ok(new MessageDTO<>(false, paymentResponse));
    }

    /**
     * Controlador para hacer el pago de una reserva de mesa
     * @param reservaId Id de la reserva
     * @return ResponseEntity con el id de la reserva
     * @throws Exception
     */
    @PostMapping("/reserva/make-payment/{reservaId}")
    public ResponseEntity<MessageDTO<PaymentResponseReservaDTO>> makePaymentReserva(@PathVariable String reservaId) throws Exception {
        PaymentResponseReservaDTO paymentResponse = reservaService.procesarPagoReserva(reservaId);
        return ResponseEntity.ok(new MessageDTO<>(false, paymentResponse));
    }

    /**
     * Controlador para crear una venta
     * @param crearVentaProductoDTO Detalle de la venta a crear
     * @return ResponseEntity con el id de la venta
     * @throws Exception
     */
    @PostMapping("/venta/create")
    public ResponseEntity<MessageDTO<String>> crearVentaProducto(@Valid @RequestBody CrearVentaProductoDTO crearVentaProductoDTO) throws Exception{
        String orderId= ventaProductoService.crearVenta(crearVentaProductoDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, orderId));
    }

    /**
     * Controlador para crear una reserva de mesa
     * @param crearReservaDTO Detalle de la reserva a crear
     * @return ResponseEntity con el id de la reserva
     * @throws Exception
     */
    @PostMapping("/reserva/create")
    public ResponseEntity<MessageDTO<String>> crearReserva(@Valid @RequestBody CrearReservaDTO crearReservaDTO) throws Exception{
        String reservaId= reservaService.reservarMesa(crearReservaDTO);
        return ResponseEntity.ok(new MessageDTO<>(false, reservaId));
    }

    /**
     * Controlador para borrar una venta
     * @param ventaProductoId Id de la venta a borrar
     * @return ResponseEntity con el id de la venta
     * @throws Exception
     */
    @DeleteMapping("/venta/cancel/{ventaProductoId}")
    public ResponseEntity<MessageDTO<String>> borrarVentaProducto(@PathVariable String ventaProductoId) throws Exception {
        String result = ventaProductoService.borrarVentaProducto(ventaProductoId);
        return ResponseEntity.ok(new MessageDTO<>(false,result));
    }

    /**
     * Controlador para cancelar una reserva
     * @param reservaId Id de la reserva a cancelar
     * @return ResponseEntity con el id de la reserva
     * @throws Exception
     */
    @DeleteMapping("/reserva/cancel/{reservaId}")
    public ResponseEntity<MessageDTO<String>> cancelarReserva(@PathVariable String reservaId) throws Exception {
        String result = reservaService.cancelarReserva(reservaId);
        return ResponseEntity.ok(new MessageDTO<>(false,result));
    }

    /**
     * Controlador para listar las ventas de un cliente
     * @param emailUsuario Email del usuario
     * @return ResponseEntity con la lista de ventas del cliente
     * @throws Exception
     */
    @GetMapping("/venta/history/{emailUsuario}")
    public ResponseEntity<MessageDTO<List<VentaItemDTO>>> listarVentasCliente(@PathVariable String emailUsuario) throws Exception{
        List<VentaItemDTO> ventas= ventaProductoService.listarVentasCliente(emailUsuario);
        return ResponseEntity.ok(new MessageDTO<>(false, ventas));
    }

    /**
     * Controlador para listar las reservas de un cliente
     * @param emailUsuario Email del usuario
     * @return ResponseEntity con la lista de reservas del cliente
     * @throws Exception
     */
    @GetMapping("/reserva/history/{emailUsuario}")
    public ResponseEntity<MessageDTO<List<ReservaItemDTO>>> listarReservasCliente(@PathVariable String emailUsuario) throws Exception {
        List<ReservaItemDTO> reservas = reservaService.listarReservasCliente(emailUsuario);
        return ResponseEntity.ok(new MessageDTO<>(false, reservas));

    }

    /**
     * Controlador para obtener la información de una venta
     * @param orderId Id de la venta
     * @return ResponseEntity con la información de la venta
     * @throws Exception
     */
    @GetMapping("/venta/get-info/{orderId}")
    public ResponseEntity<MessageDTO<VentaItemDTO>> obtenerInfoVenta(@PathVariable String orderId) throws Exception{
        VentaItemDTO ventaInfo = ventaProductoService.obtenerInformacionVenta(orderId);
        return ResponseEntity.ok(new MessageDTO<>(false, ventaInfo));
    }

    /**
     * Controlador para obtener la información de una reserva
     * @param reservaId Id de la reserva
     * @return ResponseEntity con la información de la reserva
     * @throws Exception
     */
    @GetMapping("/reserva/get-info/{reservaId}")
    public ResponseEntity<MessageDTO<ReservaItemDTO>> obtenerInfoReserva(@PathVariable String reservaId) throws Exception{
        ReservaItemDTO reservaInfo = reservaService.obtenerInformacionReserva(reservaId);
        return ResponseEntity.ok(new MessageDTO<>(false, reservaInfo));
    }
    



}
