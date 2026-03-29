package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.EmailDTO;
import co.uniquindio.tiendasana.dto.gestorReservasdtos.BorrarMesaGestorDTO;
import co.uniquindio.tiendasana.dto.reservadtos.ActualizarReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.CrearReservaDirectaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.CrearReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.PaymentResponseReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.ReservaItemDTO;
import co.uniquindio.tiendasana.model.documents.*;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.EstadoReserva;
import co.uniquindio.tiendasana.model.vo.Pago;
import co.uniquindio.tiendasana.repos.ReservasRepo;
import co.uniquindio.tiendasana.services.interfaces.*;
import com.google.firebase.database.annotations.NotNull;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.PaymentStatus;
import com.mercadopago.resources.preference.Preference;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImp implements ReservaService {
    private final MesaService mesaService;
    private final GestorReservasService gestorReservasService;
    private final EmailService emailService;
    private final CuentaService cuentaService;
    private final ReservasRepo reservasRepo;

    public ReservaServiceImp(MesaService mesaService, GestorReservasService gestorReservasService, EmailService emailService, CuentaService cuentaService, ReservasRepo reservasRepo) {
        this.mesaService = mesaService;
        this.gestorReservasService = gestorReservasService;
        this.emailService = emailService;
        this.cuentaService = cuentaService;
        this.reservasRepo = reservasRepo;
    }

    @Override
    public String reservarMesa(CrearReservaDTO crearReservaDTO) throws Exception {
        Reserva reserva = new Reserva();

        reserva.setFechaReserva(
            crearReservaDTO.fechaReserva() != null ? crearReservaDTO.fechaReserva() : LocalDateTime.now()
        );
        reserva.setUsuarioId(crearReservaDTO.emailUsuario());

        cuentaService.obtenerCuentaPorEmail(crearReservaDTO.emailUsuario());

        reserva.setId(UUID.randomUUID().toString());
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);
        reserva.setCantidadPersonas(crearReservaDTO.cantidadPersonas());

        GestorReservas gestorReservas = gestorReservasService.obtenerGestorReservas(crearReservaDTO.emailUsuario());
        List<Mesa> items = obtenerMesas(gestorReservas, reserva.getId());
        reserva.setMesas(items);


        reserva.setValorReserva(calcularTotal(items, crearReservaDTO.emailUsuario()));


        Reserva createOrder = reservasRepo.guardarReserva(reserva);

        return createOrder.getId();
    }

    @Override
    public String reservarMesaDirecta(CrearReservaDirectaDTO crearReservaDirectaDTO) throws Exception {
        Reserva reserva = new Reserva();

        reserva.setFechaReserva(crearReservaDirectaDTO.fechaReserva());
        reserva.setUsuarioId(crearReservaDirectaDTO.emailUsuario());
        cuentaService.obtenerCuentaPorEmail(crearReservaDirectaDTO.emailUsuario());

        reserva.setId(UUID.randomUUID().toString());
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);
        reserva.setCantidadPersonas(crearReservaDirectaDTO.cantidadPersonas());

        Mesa mesa = mesaService.obtenerMesa(crearReservaDirectaDTO.mesaId());
        if (!EstadoMesa.DISPONIBLE.getEstado().equalsIgnoreCase(mesa.getEstado())) {
            throw new IllegalStateException("La mesa seleccionada no se encuentra disponible para reservar");
        }

        Mesa mesaReservada = new Mesa();
        mesaReservada.setId(mesa.getId());
        mesaReservada.setNombre(mesa.getNombre());
        mesaReservada.setEstado(EstadoMesa.RESERVADA);
        mesaReservada.setLocalidad(mesa.getLocalidad());
        mesaReservada.setPrecioReserva(mesa.getPrecioReserva());
        mesaReservada.setImagen(mesa.getImagen());
        mesaReservada.setIdReserva(reserva.getId());
        mesaReservada.setIdGestorReserva("-");
        mesaReservada.setCapacidad(mesa.getCapacidad());

        List<Mesa> items = List.of(mesaReservada);
        reserva.setMesas(items);
        reserva.setValorReserva(calcularTotal(items, crearReservaDirectaDTO.emailUsuario()));

        Reserva createOrder = reservasRepo.guardarReserva(reserva);
        return createOrder.getId();
    }

    private float calcularTotal(List<Mesa> items, String s) {
        float total = 0;
        for (Mesa mesa : items) {
            total += mesa.getPrecioReserva();
        }
        return total;

    }

    private List<Mesa> obtenerMesas(GestorReservas gestorReservas, String id) {
        List<Mesa> items = new ArrayList<>();
        List<Mesa> details = gestorReservas.getMesas();
        details.forEach(mesaAReservar -> {
            try {

                Mesa mesa = mesaService.obtenerMesa(String.valueOf(mesaAReservar.getId()));

                Mesa mesaReservada = new Mesa();
                mesaReservada.setId(mesa.getId());
                mesaReservada.setNombre(mesaAReservar.getNombre());
                mesaReservada.setEstado(EstadoMesa.RESERVADA);
                mesaReservada.setLocalidad(mesaAReservar.getLocalidad());
                mesaReservada.setPrecioReserva(mesaAReservar.getPrecioReserva());
                mesaReservada.setImagen(mesaAReservar.getImagen());
                mesaReservada.setIdReserva(id);
                mesaReservada.setIdGestorReserva("-");
                mesaReservada.setCapacidad(mesaAReservar.getCapacidad());
                items.add(mesaReservada);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        });
        return items;
    }

    @Override
    public Reserva obtenerReserva(String idReserva) throws Exception {
        List<Reserva> reservas = reservasRepo.filtrarReservasSimple(reservaFiltrada -> {
            return reservaFiltrada.getId().equals(idReserva); });
        if (reservas.isEmpty()) {
            throw new Exception("La reserva con el id: " + idReserva + " no existe");
        }
        return reservas.get(0);
    }

    @Override
    public String cancelarReserva(String idReserva) throws Exception {
        Reserva reservaCancelar = obtenerReserva(idReserva);
        reservaCancelar.setEstadoReserva(EstadoReserva.CANCELADA);
        reservasRepo.actualizarReservaSimple(reservaCancelar);
        return "La reserva fue cancelada y se le retorná el dinero a su cuenta de MercadoPago";
    }

    @Override
    public String actualizarReserva(ActualizarReservaDTO actualizarReservaDTO) throws Exception {
        Reserva reservaActualizar = obtenerReserva(actualizarReservaDTO.reservaId());
        reservaActualizar.setCantidadPersonas(actualizarReservaDTO.cantidadPersonas());
        reservaActualizar.setFechaReserva(actualizarReservaDTO.fechaReserva());
        reservasRepo.actualizarReservaSimple(reservaActualizar);
        return "La reserva fue actualizada";
    }

    @Override
    public ReservaItemDTO obtenerInformacionReserva(String idReserva) throws Exception {
        Reserva reserva = obtenerReserva(idReserva); // Método que obtiene la orden

        return mapearAReservaItemDTO(reserva);
    }

    private ReservaItemDTO mapearAReservaItemDTO(Reserva reserva) {
        return new ReservaItemDTO(
                reserva.getUsuarioId() != null ? reserva.getUsuarioId().toString() : null,
                reserva.getFechaReserva(),
                reserva.getEstadoReserva().toString(),
                reserva.getPago() != null ? reserva.getPago().getPaymentType() : null,
                reserva.getPago() != null ? reserva.getPago().getStatus() : null,
                reserva.getPago() != null ? reserva.getPago().getDate() : null,
                reserva.getPago() != null ? reserva.getPago().getTransactionValue() : 0f,
                reserva.getId(),
                reserva.getValorReserva(),
                reserva.getCantidadPersonas(),
                reserva.getMesas());
    }

    @Override
    public List<ReservaItemDTO> listarReservasCliente(String clienteId) throws IOException {
        List<Reserva> reservas = reservasRepo.filtrarReservasSimple(reserva -> reserva.getUsuarioId().equals(clienteId));
        System.out.println("Reservas: " + reservas);
        return obtenerReservaItemDTO(reservas);
    }

    private List<ReservaItemDTO> obtenerReservaItemDTO(List<Reserva> reservas) {
        return reservas.stream().map(this::mapearAReservaItemDTO
        ).collect(Collectors.toList());
    }

    @Override
    public PaymentResponseReservaDTO procesarPagoReserva(String idReserva) throws Exception {
        try {
            Reserva reservaGuardar = obtenerReserva(idReserva);

            List<PreferenceItemRequest> itemsGateway = new ArrayList<>();

            for (Mesa item : reservaGuardar.getMesas()) {
                Mesa mesa = mesaService.obtenerMesa(item.getId());

                // Usar el precio de la mesa como unitPrice
                PreferenceItemRequest itemRequest =
                        PreferenceItemRequest.builder()
                                .id(mesa.getId())
                                .title(mesa.getNombre())
                                .pictureUrl(mesa.getImagen())
                                .categoryId(mesa.getLocalidad().toString())
                                .quantity(1) // Asegúrate de que la cantidad sea correcta
                                .currencyId("COP")
                                .unitPrice(BigDecimal.valueOf(mesa.getPrecioReserva())) // Cambiado aquí
                                .build();
                itemsGateway.add(itemRequest);
            }

            MercadoPagoConfig.setAccessToken("APP_USR-8178646482281064-100513-248819fc76ea7f7577f902e927eaefb7-2014458486");

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://abad-2803-9810-51a4-a910-95ae-3eca-e425-fddf.ngrok-free.app/?status=success")
                    .failure("https://abad-2803-9810-51a4-a910-95ae-3eca-e425-fddf.ngrok-free.app/?status=failure")
                    .pending("https://abad-2803-9810-51a4-a910-95ae-3eca-e425-fddf.ngrok-free.app/?status=pending")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .backUrls(backUrls)
                    .items(itemsGateway)
                    .metadata(Map.of("id_reserva", reservaGuardar.getId()))
                    .notificationUrl("https://30cc-152-202-214-200.ngrok-free.app/api/public/reserva/receive-notification")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            reservaGuardar.setCodigoPasarela(preference.getId());
            reservasRepo.actualizarReservaSimple(reservaGuardar);

            return new PaymentResponseReservaDTO(
                    preference.getInitPoint(),
                    idReserva
            );
        } catch (MPApiException e) {
            if (e.getApiResponse() != null && e.getApiResponse().getContent() != null) {
                System.err.println("API Response Content: " + e.getApiResponse().getContent());
            }
            e.printStackTrace();
            throw new Exception("Error al crear la preferencia de pago con Mercado Pago: " + e.getMessage() + ". Detalles: " + (e.getApiResponse() != null ? e.getApiResponse().getContent() : "No additional details"), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error general al procesar el pago de la reserva: " + e.getMessage(), e);
        }
    }

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
                String idReserva = payment.getMetadata().get("id_reserva").toString();

                // Se obtiene la orden guardada en la base de datos y se le asigna el pago, ademas de aumentar la cantidad de entradas vendidas
                Reserva reserva = obtenerReserva(idReserva);

                Pago orderPago = createPayment(payment);

                reserva.setPago(orderPago);
                reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
                reservasRepo.actualizarReservaSimple(reserva);
                Cuenta cuenta = cuentaService.obtenerCuentaPorEmail(reserva.getUsuarioId());


                if (reserva.getPago().getStatus().equalsIgnoreCase("APPROVED") && reserva.getPago().getStatusDetail().equalsIgnoreCase("accredited")) {

                    for (Mesa mesa : reserva.getMesas()){
                        mesaService.cambiarEstadoMesa(mesa.getId(), "Reservada");
                        try {
                            gestorReservasService.borrarMesaGestorReservas(new BorrarMesaGestorDTO(reserva.getUsuarioId(), mesa.getId()));
                        } catch (Exception ignored) {
                            // Reserva directa: puede no existir en el gestor y no debe bloquear confirmación/persistencia.
                        }
                    }

                    System.out.println("SE ESTÄ INTENTANDO ENVIAR EL CORREO A " + cuenta.getEmail());
                    enviarResumenReserva(cuenta.getEmail(), reserva);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String enviarResumenReserva(String email, Reserva reserva) throws ResourceNotFoundException, Exception {
        Cuenta cuenta = cuentaService.obtenerCuentaPorEmail(email);

        String qrCodeUrl = "https://quickchart.io/qr?text=" + reserva.getId() + "&size=300";
        byte[] qrCodeImage = emailService.downloadImage(qrCodeUrl);

        String subject = "Resumen de tu reserva en Tienda Sana";
        StringBuilder body = new StringBuilder();

        // Inicio del HTML con estilos CSS incorporados
        body.append("<!DOCTYPE html>");
        body.append("<html>");
        body.append("<head>");
        body.append("<meta charset='UTF-8'>");
        body.append("<style>");
        body.append("body { font-family: 'Helvetica', Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0; }");
        body.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f8f8; }");
        body.append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }");
        body.append(".content { background-color: #ffffff; padding: 20px; border-radius: 0 0 5px 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        body.append("h1 { margin: 0; color: white; font-size: 24px; }");
        body.append("h2 { color: #4CAF50; font-size: 20px; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; margin-top: 20px; }");
        body.append(".mesa { background-color: #f9f9f9; padding: 15px; margin: 10px 0; border-left: 4px solid #4CAF50; border-radius: 4px; }");
        body.append(".total { background-color: #4CAF50; color: white; padding: 10px; text-align: right; margin-top: 20px; font-size: 18px; border-radius: 4px; }");
        body.append(".footer { margin-top: 20px; font-size: 12px; text-align: center; color: #777; }");
        body.append(".qr-code { text-align: center; margin: 20px 0; }");
        body.append("</style>");
        body.append("</head>");

        // Contenido del correo
        body.append("<body>");
        body.append("<div class='container'>");

        // Encabezado
        body.append("<div class='header'>");
        body.append("<h1>Tienda Sana</h1>");
        body.append("</div>");

        // Contenido principal
        body.append("<div class='content'>");
        body.append("<h2>¡Hola ").append(cuenta.getUsuario().getNombre()).append("!</h2>");
        body.append("<p>Gracias por tu reserva en Tienda Sana. A continuación encontrarás un resumen de tu reserva:</p>");

        // Detalles de la reserva
        body.append("<h2>Detalles de la Reserva:</h2>");
        body.append("<p><strong>Número de Reserva:</strong> #").append(reserva.getId()).append("<br>");
        body.append("<strong>Fecha de Reserva:</strong> ").append(reserva.getFechaReserva()).append("<br>");
        body.append("<strong>Estado de la Reserva:</strong> ").append(reserva.getEstadoReserva()).append("</p>");

        // Información de pago
        if (reserva.getPago() != null) {
            body.append("<p><strong>Método de Pago:</strong> ").append(traducirMetodoPago(reserva.getPago().getPaymentType())).append("<br>");
            body.append("<strong>Estado del Pago:</strong> ").append(traducirEstadoPago(reserva.getPago().getStatus())).append("</p>");
        }

        // Mesas reservadas
        body.append("<h2>Mesas Reservadas:</h2>");
        for (Mesa mesa : reserva.getMesas()) {
            body.append("<div class='mesa'>");
            body.append("<p><strong>").append(mesa.getNombre()).append("</strong><br>");
            body.append("Localidad: ").append(mesa.getLocalidad()).append("<br>");
            body.append("Capacidad: ").append(mesa.getCapacidad()).append("<br>");
            body.append("Precio de Reserva: $").append(formatearPrecio(mesa.getPrecioReserva())).append("</p>");
            body.append("</div>");
        }

        // Total
        body.append("<div class='total'>");
        body.append("Total pagado: $").append(formatearPrecio(reserva.getValorReserva()));
        body.append("</div>");

        // Código QR
        body.append("<div class='qr-code'>");
        body.append("<p><strong>Código de confirmación</strong></p>");
        body.append("<img src='cid:qrCodeImage' alt='Código QR de tu reserva' width='150' height='150'>");
        body.append("<p>Presenta este código para acceder a tu reserva</p>");
        body.append("</div>");

        // Pie de página
        body.append("<div class='footer'>");
        body.append("<p>Si tienes alguna duda sobre tu reserva, no dudes en contactarnos a través de <a href='mailto:soporte@tiendasana.com'>soporte@tiendasana.com</a>.</p>");
        body.append("<p>© ").append(java.time.Year.now().getValue()).append(" Tienda Sana. Todos los derechos reservados.</p>");
        body.append("</div>");

        body.append("</div>"); // Cierre del div content
        body.append("</div>"); // Cierre del div container
        body.append("</body>");
        body.append("</html>");

        // Enviar el correo con la imagen embebida
        emailService.sendEmailHtmlWithAttachment(new EmailDTO(subject, body.toString(), email), qrCodeImage, "qrCodeImage");

        return "El resumen de tu reserva ha sido enviado a tu correo electrónico";
    }

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


    // Métodos auxiliares para traducción
    private String traducirMetodoPago(String paymentType) {
        switch (paymentType.toLowerCase()) {
            case "credit_card":
                return "Tarjeta de Crédito";
            case "debit_card":
                return "Tarjeta de Débito";
            case "paypal":
                return "PayPal";
            case "cash":
                return "Efectivo";
            default:
                return paymentType;
        }
    }

    private String traducirEstadoPago(String status) {
        switch (status.toLowerCase()) {
            case "completed":
                return "Completado";
            case "pending":
                return "Pendiente";
            case "failed":
                return "Fallido";
            case "refunded":
                return "Reembolsado";
            default:
                return status;
        }
    }

    private String formatearPrecio(double precio) {
        return String.format("%.2f", precio);
    }
}
