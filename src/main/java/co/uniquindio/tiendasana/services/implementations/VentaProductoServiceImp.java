package co.uniquindio.tiendasana.services.implementations;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import co.uniquindio.tiendasana.dto.EmailDTO;
import co.uniquindio.tiendasana.dto.ventadtos.CrearVentaProductoDTO;
import co.uniquindio.tiendasana.dto.ventadtos.DetalleOrdenDTO;
import co.uniquindio.tiendasana.dto.ventadtos.PaymentResponseDTO;
import co.uniquindio.tiendasana.dto.ventadtos.VentaItemDTO;
import co.uniquindio.tiendasana.model.documents.*;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import co.uniquindio.tiendasana.model.vo.DetalleVentaProducto;
import co.uniquindio.tiendasana.model.vo.Pago;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.payment.PaymentStatus;
import com.mercadopago.resources.preference.Preference;
import co.uniquindio.tiendasana.repos.VentaProductoRepo;
import co.uniquindio.tiendasana.services.interfaces.*;
import com.google.firebase.database.annotations.NotNull;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaProductoServiceImp implements VentaProductoService {
    private final ProductoService productoService;
    private final CarritoComprasService carritoComprasService;
    private final EmailService emailService;
    private final CuentaService cuentaService;
    private final PromocionService promocionService;
    private final VentaProductoRepo ventaProductoRepo;


    public VentaProductoServiceImp(CuentaService cuentaService, ProductoService productoService, CarritoComprasService carritoComprasService, EmailService emailService, PromocionService promocionService, VentaProductoRepo ventaProductoRepo) {
        this.cuentaService = cuentaService;
        this.productoService = productoService;
        this.carritoComprasService = carritoComprasService;
        this.emailService = emailService;
        this.promocionService = promocionService;

        this.ventaProductoRepo = ventaProductoRepo;
    }

    /**
     * Método para crear una venta de productos
     * @param crearVentaProductoDTO DTO con la información de la venta
     * @return ID de la venta creada
     * @throws Exception
     */
    @Override
    public String crearVenta(CrearVentaProductoDTO crearVentaProductoDTO) throws Exception {

        CarritoCompras carritoCompras = carritoComprasService.getCarritoCompras(crearVentaProductoDTO.clienteId());
        List<DetalleVentaProducto> items = getOrderDetails(carritoCompras);

        VentaProducto ventaProducto = new VentaProducto();
        ventaProducto.setProductos(items);
        ventaProducto.setFecha(LocalDateTime.now());
        ventaProducto.setUsuarioId(crearVentaProductoDTO.clienteId());

        if (crearVentaProductoDTO.idPromocion() != null && !crearVentaProductoDTO.idPromocion().isEmpty()) {

            Promocion promocion = promocionService.getPromocion(crearVentaProductoDTO.idPromocion());

            if (promocion == null) {
                throw new ResourceNotFoundException("La promocion no existe");
            }

            // Validar que el cupón tiene un id válido antes de usarlo
            if (promocion.getId() == null) {
                throw new ResourceNotFoundException("La promocion no tiene un id válido para crear la orden");
            }


            float totalWithDiscount = calculateTotal(items, promocion.getId(), crearVentaProductoDTO.clienteId());
            ventaProducto.setTotal(totalWithDiscount);


        } else {
            ventaProducto.setTotal(calculateTotal(items, null, crearVentaProductoDTO.clienteId()));
        }

        Cuenta cuenta = cuentaService.obtenerCuenta(crearVentaProductoDTO.clienteId());
        VentaProducto createOrder = ventaProductoRepo.guardar(ventaProducto);

        enviarResumenVenta(cuenta.getEmail(), ventaProducto);

        carritoComprasService.borrarCarritoCompras(crearVentaProductoDTO.clienteId());

        return createOrder.getId();
    }


    /**
     * Método para obtener los detalles de la orden a partir del carrito de compras
     * @param carritoCompras Carrito de compras del cliente
     * @return Lista de detalles de la orden
     */
    private @NotNull List<DetalleVentaProducto> getOrderDetails(CarritoCompras carritoCompras) {
        List<DetalleVentaProducto> items = new ArrayList<>();
        List<DetalleCarrito> details = carritoCompras.getProductos();
        details.forEach(carDetail -> {
            try {

                Producto producto = productoService.getProducto(String.valueOf(carDetail.getProductoId()));

                DetalleVentaProducto orderDetail = new DetalleVentaProducto();
                orderDetail.setProductoId(carDetail.getProductoId());
                orderDetail.setValor(carDetail.getSubtotal() * carDetail.getCantidad());
                orderDetail.setCantidad(carDetail.getCantidad());
                items.add(orderDetail);



            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        });
        return items;
    }

    /**
     * Método para calcular el total de la venta
     * @param items Lista de detalles de la venta
     * @param promocionId ID de la promoción
     * @param usuarioId ID del usuario
     * @return Total de la venta
     */
    private float calculateTotal(List<DetalleVentaProducto> items, String promocionId, String usuarioId) {
        if (promocionId != null && !promocionId.isEmpty()) {

            Promocion promocion = promocionService.getPromocion(promocionId);

            if (promocion == null) {
                throw new ResourceNotFoundException("Coupon not found with id: " + promocionId);
            }

            float total = 0;
            for (DetalleVentaProducto detalleVentaProducto : items) {
                total += detalleVentaProducto.getValor();
            }


            return total * (1 - promocion.getPorcentajeDescuento());
        } else {
            float total = 0;
            for (DetalleVentaProducto detalleVentaProducto : items) {
                total += detalleVentaProducto.getValor();
            }
            return total;
        }
    }

    /**
     * Método para obtener las ventas de un cliente
     * @param idClient ID del cliente
     * @return Lista de ventas del cliente
     */
    private List<VentaProducto> obtenerVentasProductoPorCliente(String idClient) {
        return ventaProductoRepo.obtenerVentasPorCliente(idClient);
    }

    /**
     * Método para obtener una venta de producto por su ID
     * @param idVentaProducto ID de la venta de producto
     * @return Venta de producto
     * @throws ResourceNotFoundException
     */
    @Override
    public VentaProducto obtenerVentaProducto(String idVentaProducto) throws ResourceNotFoundException {
        VentaProducto ventaProducto = ventaProductoRepo.obtenerVentaProducto(idVentaProducto);
        if (ventaProducto == null) {
            throw new ResourceNotFoundException("La venta con el id: " + idVentaProducto + " no existe");
        }
        return ventaProducto;
    }

    /**
     * Método para borrar una venta de producto
     * @param idVentaProducto ID de la venta de producto
     * @return Mensaje de éxito
     * @throws Exception
     */
    @Override
    public String borrarVentaProducto(String idVentaProducto) throws Exception {
        VentaProducto ventaBorrar = obtenerVentaProducto(idVentaProducto);
        Pago pago = ventaBorrar.getPago();
        if(pago ==null || (!(pago.getStatus().equals(PaymentStatus.APPROVED) && pago.getStatusDetail().equalsIgnoreCase("accredited"))) ){
            ventaProductoRepo.borrar(ventaBorrar);
            return "La venta fue borrada";
        }else{
            throw new Exception("No se puede borrar la venta porque ya fue aprobada");
        }
    }

    /**
     * Método para obtener la información de una venta
     * @param ventaProductoId ID de la venta de producto
     * @return Información de la venta
     * @throws ResourceNotFoundException
     */
    @Override
    public VentaItemDTO obtenerInformacionVenta(String ventaProductoId) throws ResourceNotFoundException {
        VentaProducto ventaProducto = obtenerVentaProducto(ventaProductoId); // Método que obtiene la orden

        return mapearAVentaItemDTO(ventaProducto);
    }

    /**
     * Método para mapear una venta de producto a un DTO
     * @param ventaProducto Venta de producto
     * @return DTO de la venta
     */
    private VentaItemDTO mapearAVentaItemDTO(VentaProducto ventaProducto) {
        return new VentaItemDTO(
                ventaProducto.getUsuarioId() != null ? ventaProducto.getUsuarioId().toString() : null,
                ventaProducto.getFecha(),
                mapearADetalleVentaProducto(ventaProducto.getProductos()),
                ventaProducto.getPago() != null ? ventaProducto.getPago().getPaymentType() : null,
                ventaProducto.getPago() != null ? ventaProducto.getPago().getStatus() : null,
                ventaProducto.getPago() != null ? ventaProducto.getPago().getDate() : null,
                ventaProducto.getPago() != null ? ventaProducto.getPago().getTransactionValue() : 0f,
                ventaProducto.getId(),
                ventaProducto.getTotal(),
                ventaProducto.getPromocionId() != null ? ventaProducto.getPromocionId() : null);
    }

    /**
     * Método para listar las ventas de un cliente
     * @param clienteId ID del cliente
     * @return Lista de ventas del cliente
     */
    @Override
    public List<VentaItemDTO> listarVentasCliente(String clienteId) {
        List<VentaProducto> ventas = ventaProductoRepo.obtenerVentasPorCliente(clienteId);
        return obtenerVentaItemDto(ventas);
    }

    /**
     * Método para mapear una lista de ventas a un DTO
     * @param ventas Lista de ventas
     * @return Lista de DTOs de ventas
     */
    @NotNull
    private List<VentaItemDTO> obtenerVentaItemDto(List<VentaProducto> ventas) {
        return ventas.stream().map(this::mapearAVentaItemDTO
        ).collect(Collectors.toList());
    }

    /**
     * Método para mapear una lista de productos a un DTO
     * @param productos Lista de productos
     * @return Lista de DTOs de productos
     */
    private @NotNull List<DetalleOrdenDTO> mapearADetalleVentaProducto(List<DetalleVentaProducto> productos) {
        return productos.stream().map(e -> new DetalleOrdenDTO(
                e.getProductoId(),
                e.getValor(),
                e.getCantidad()
        )).collect(Collectors.toList());
    }

    /**
     * Método para realizar el pago de una venta
     * @param ventaProductoId ID de la venta de producto
     * @return Respuesta de la pasarela de pago
     * @throws Exception
     */
    @Override
    public PaymentResponseDTO makePayment(String ventaProductoId) throws Exception {
        // Obtener la orden guardada en la base de datos y los ítems de la orden
        VentaProducto ventaGuardar = obtenerVentaProducto(ventaProductoId);
        List<PreferenceItemRequest> itemsGateway = new ArrayList<>();

        // Comprobar si hay un cupón de descuento en la orden
        Promocion promocion = null;
        if (ventaGuardar.getPromocionId() != null) {
            promocion = promocionService.getPromocion(ventaGuardar.getPromocionId());
        }
        List<VentaProducto> ventasCliente = obtenerVentasProductoPorCliente(ventaGuardar.getUsuarioId());

        // Recorrer los items de la orden y crea los ítems de la pasarela
        for (DetalleVentaProducto item : ventaGuardar.getProductos()) {
            // Obtener el evento y la localidad del ítem
            Producto producto = productoService.getProducto(item.getProductoId());

            float unitPrice = (promocion != null) ?
                    Math.max(0, producto.getPrecioUnitario() - (producto.getPrecioUnitario() * promocion.getPorcentajeDescuento())) :
                    producto.getPrecioUnitario();


            // Crear el item de la pasarela
            PreferenceItemRequest itemRequest =
                    PreferenceItemRequest.builder()
                            .id(producto.getId())
                            .title(producto.getNombre())
                            .pictureUrl(producto.getImagen())
                            .categoryId(producto.getCategoria()) // Cambiar a categoryId
                            .quantity(item.getCantidad())
                            .currencyId("COP")
                            .unitPrice(BigDecimal.valueOf(unitPrice))
                            .build();
            itemsGateway.add(itemRequest);


        }

        //TODO Configurar las credenciales de MercadoPag. Crear cuenta de mercado pago
        MercadoPagoConfig.setAccessToken("APP_USR-8178646482281064-100513-248819fc76ea7f7577f902e927eaefb7-2014458486");

        //TODO
        // Configurar las urls de retorno de la pasarela (Frontend)
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("https://smooth-unicorn-trusting.ngrok-free.app/?status=success")
                .failure("https://smooth-unicorn-trusting.ngrok-free.app/?status=failure")
                .pending("https://smooth-unicorn-trusting.ngrok-free.app/?status=pending")
                .build();


        // Construir la preferencia de la pasarela con los ítems, metadatos y urls de retorno
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .backUrls(backUrls)
                .items(itemsGateway)
                //TODO agregar id orden
                .metadata(Map.of("id_orden", ventaGuardar.getId()))
                //TODO Agregar url de Ngrok (Se actualiza constantemente) la ruta debe incluir la direccion al controlador de las notificaciones
                .notificationUrl("https://smooth-unicorn-trusting.ngrok-free.app/api/public/order/receive-notification")
                .build();


        // Crear la preferencia en la pasarela de MercadoPago
        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);


        // Guardar el código de la pasarela en la orden
        ventaGuardar.setCodigoPasarela(preference.getId());
        ventaProductoRepo.guardar(ventaGuardar);



        return new PaymentResponseDTO(
                preference.getInitPoint(),
                ventaProductoId
        );
    }

    /**
     * Método para recibir notificaciones de MercadoPago
     * @param request Notificación de MercadoPago
     */
    @Override
    public void receiveNotificationFromMercadoPago(Map<String, Object> request) {
        try {
            // Obtener el tipo de notificación
            Object tipo = request.get("type");

            // Si la notificación es de un pago entonces obtener el pago y la orden asociada
            if ("payment".equals(tipo)) {
                // Capturamos el JSON que viene en el request y lo convertimos a un String
                String input = request.get("data").toString();

                // Extraemos los números de la cadena, es decir, el id del pago
                String idPago = input.replaceAll("\\D+", "");

                // Se crea el cliente de MercadoPago y se obtiene el pago con el id
                PaymentClient client = new PaymentClient();
                com.mercadopago.resources.payment.Payment payment = client.get(Long.parseLong(idPago));

                // Obtener el id de la orden asociada al pago que viene en los metadatos
                String idOrden = payment.getMetadata().get("id_orden").toString();

                // Se obtiene la orden guardada en la base de datos y se le asigna el pago, ademas de aumentar la cantidad de entradas vendidas
                VentaProducto ventaProducto = obtenerVentaProducto(idOrden);
                Pago orderPago = createPayment(payment);

                ventaProducto.setPago(orderPago);
                ventaProductoRepo.guardar(ventaProducto);
                Cuenta cuenta = cuentaService.obtenerCuenta(ventaProducto.getUsuarioId());

                List<VentaProducto> ordersClient = obtenerVentasProductoPorCliente(cuenta.getId());
                if (ventaProducto.getPago().getStatus().equalsIgnoreCase("APPROVED") && ventaProducto.getPago().getStatusDetail().equalsIgnoreCase("accredited")) {
                    enviarResumenVenta(cuenta.getEmail(), ventaProducto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Método para crear un objeto Pago a partir de un objeto Payment
     * @param payment Objeto Payment de MercadoPago
     * @return Objeto Pago
     */
    private Pago createPayment(com.mercadopago.resources.payment.Payment payment) {
        Pago orderPayment = new Pago();
        orderPayment.setId(payment.getId().toString());
        orderPayment.setDate(payment.getDateCreated().toLocalDateTime());
        orderPayment.setStatus(payment.getStatus());
        orderPayment.setStatusDetail(payment.getStatusDetail());
        orderPayment.setPaymentType(payment.getPaymentTypeId());
        orderPayment.setCurrency(payment.getCurrencyId());
        orderPayment.setAuthorizationCode(payment.getAuthorizationCode());
        orderPayment.setTransactionValue(payment.getTransactionAmount().floatValue());
        return orderPayment;
    }

    /**
     * Método para enviar un resumen de la venta al correo del cliente
     * @param email Correo del cliente
     * @param ventaProducto Venta de producto
     * @return Mensaje de éxito
     * @throws ResourceNotFoundException
     * @throws Exception
     */
    public String enviarResumenVenta(String email, VentaProducto ventaProducto) throws ResourceNotFoundException, Exception {
        Cuenta cuenta = cuentaService.obtenerCuentaPorEmail(email);

        String qrCodeUrl = "https://quickchart.io/qr?text=" + ventaProducto.getId() + "&size=300";
        byte[] qrCodeImage = emailService.downloadImage(qrCodeUrl);

        String subject = "Summary of your purchase";
        StringBuilder body = new StringBuilder();

        body.append("<html><body>");
        body.append("<h1>Hello ").append(cuenta.getUsuario().getNombre()).append("!</h1>");
        body.append("<p>Thank you for your purchase. Below is a summary of your order:</p>");

        body.append("<h3>Order Summary:</h3>");
        body.append("<p>Order Number: ").append(ventaProducto.getId()).append("<br>") //El correo se está enviando antes de crear la orden por lo q
                .append("Purchase Date: ").append(ventaProducto.getFecha()).append("</p>");

        if (ventaProducto.getPago() != null) {
            body.append("<p>Payment Method: ").append(ventaProducto.getPago().getPaymentType().toLowerCase()).append("<br>")
                    .append("Payment Status: ").append(ventaProducto.getPago().getStatus()).append("</p>");
        }

        body.append("<h3>Event Details:</h3>");
        for (DetalleVentaProducto item : ventaProducto.getProductos()) {
            Producto producto = productoService.getProducto(item.getProductoId());
            body.append("<p>---------------------------------<br>")
                    .append("Producto: ").append(producto.getNombre()).append("<br>")
                    .append("Descripcion: ").append(producto.getDescripcion()).append("<br>")
                    .append("Cantidad: ").append(item.getCantidad()).append("<br>")
                    .append("---------------------------------</p>");
        }

        body.append("<p>Total Paid: ").append(ventaProducto.getTotal()).append("</p>");


        body.append("</body></html>");

        // Enviar el correo con la imagen embebida
        emailService.sendEmailHtmlWithAttachment(new EmailDTO(subject, body.toString(), email), qrCodeImage, "qrCodeImage");

        return "The summary of your purchase has been sent to your email";
    }






}
