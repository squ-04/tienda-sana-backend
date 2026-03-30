package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.EmailDTO;
import co.uniquindio.tiendasana.dto.gestorReservasdtos.BorrarMesaGestorDTO;
import co.uniquindio.tiendasana.dto.mesadtos.MesaHorarioReservadoDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImp implements ReservaService {
    private static final int DURACION_RESERVA_MINUTOS_DEFAULT = 120;

    private final MesaService mesaService;
    private final GestorReservasService gestorReservasService;
    private final EmailService emailService;
    private final CuentaService cuentaService;
    private final ReservasRepo reservasRepo;
    private final long pendingReservationHoldMinutes;
    private final String mercadoPagoAccessToken;
    private final String mercadoPagoFrontendBaseUrl;
    private final String mercadoPagoWebhookBaseUrl;

    public ReservaServiceImp(MesaService mesaService, GestorReservasService gestorReservasService, EmailService emailService, CuentaService cuentaService, ReservasRepo reservasRepo,
                             @Value("${reservas.pending-hold-minutes:15}") long pendingReservationHoldMinutes,
                             @Value("${mercadopago.access-token:}") String mercadoPagoAccessToken,
                             @Value("${mercadopago.frontend-base-url:http://localhost:4200}") String mercadoPagoFrontendBaseUrl,
                             @Value("${mercadopago.webhook-base-url:http://localhost:8080}") String mercadoPagoWebhookBaseUrl) {
        this.mesaService = mesaService;
        this.gestorReservasService = gestorReservasService;
        this.emailService = emailService;
        this.cuentaService = cuentaService;
        this.reservasRepo = reservasRepo;
        this.pendingReservationHoldMinutes = Math.max(pendingReservationHoldMinutes, 1);
        this.mercadoPagoAccessToken = mercadoPagoAccessToken;
        this.mercadoPagoFrontendBaseUrl = sanitizeBaseUrl(mercadoPagoFrontendBaseUrl);
        this.mercadoPagoWebhookBaseUrl = sanitizeBaseUrl(mercadoPagoWebhookBaseUrl);
    }

    @Override
    public String reservarMesa(CrearReservaDTO crearReservaDTO) throws Exception {
        Reserva reserva = new Reserva();

        LocalDateTime inicioReserva =
            crearReservaDTO.fechaReserva() != null ? crearReservaDTO.fechaReserva() : LocalDateTime.now();

        reserva.setFechaCreacion(LocalDateTime.now());
        reserva.setFechaReserva(inicioReserva);
        reserva.setUsuarioId(crearReservaDTO.emailUsuario());
        cuentaService.obtenerCuentaPorEmail(crearReservaDTO.emailUsuario());

        reserva.setId(UUID.randomUUID().toString());
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);
        reserva.setCantidadPersonas(crearReservaDTO.cantidadPersonas());

        GestorReservas gestorReservas = gestorReservasService.obtenerGestorReservas(crearReservaDTO.emailUsuario());
        List<Mesa> items = obtenerMesas(gestorReservas, reserva.getId());
        int duracionReservaMinutos = resolveDuracionReservaMinutos(items);
        LocalDateTime finReserva = inicioReserva.plusMinutes(duracionReservaMinutos);
        reserva.setFechaFinReserva(finReserva);
        validarCruceHorarios(items, inicioReserva, finReserva, null);
        reserva.setMesas(items);

        reserva.setValorReserva(calcularTotal(items, crearReservaDTO.emailUsuario()));

        Reserva createOrder = reservasRepo.guardarReserva(reserva);
        return createOrder.getId();
    }

    @Override
    public String reservarMesaDirecta(CrearReservaDirectaDTO crearReservaDirectaDTO) throws Exception {
        Reserva reserva = new Reserva();

        LocalDateTime inicioReserva = crearReservaDirectaDTO.fechaReserva();

        reserva.setFechaCreacion(LocalDateTime.now());
        reserva.setFechaReserva(inicioReserva);
        reserva.setUsuarioId(crearReservaDirectaDTO.emailUsuario());
        cuentaService.obtenerCuentaPorEmail(crearReservaDirectaDTO.emailUsuario());

        reserva.setId(UUID.randomUUID().toString());
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);
        reserva.setCantidadPersonas(crearReservaDirectaDTO.cantidadPersonas());

        Mesa mesa = mesaService.obtenerMesa(crearReservaDirectaDTO.mesaId());
        int duracionReservaMinutos = resolveDuracionReservaMinutos(List.of(mesa));
        LocalDateTime finReserva = inicioReserva.plusMinutes(duracionReservaMinutos);
        reserva.setFechaFinReserva(finReserva);

        Mesa mesaReservada = new Mesa();
        mesaReservada.setId(mesa.getId());
        mesaReservada.setNombre(mesa.getNombre());
        mesaReservada.setEstado(EstadoMesa.RESERVADA);
        mesaReservada.setLocalidad(mesa.getLocalidad());
        mesaReservada.setPrecioReserva(mesa.getPrecioReserva());
        mesaReservada.setImagen(mesa.getImagen());
        mesaReservada.setDuracionReservaMinutos(mesa.getDuracionReservaMinutos());
        mesaReservada.setIdReserva(reserva.getId());
        mesaReservada.setIdGestorReserva("-");
        mesaReservada.setCapacidad(mesa.getCapacidad());

        List<Mesa> items = List.of(mesaReservada);
        validarCruceHorarios(items, inicioReserva, finReserva, null);
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
                mesaReservada.setDuracionReservaMinutos(mesa.getDuracionReservaMinutos());
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
        List<Reserva> reservas = reservasRepo.filtrarReservasSimple(reservaFiltrada -> reservaFiltrada.getId().equals(idReserva));
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
        LocalDateTime inicioNuevo = actualizarReservaDTO.fechaReserva();
        LocalDateTime finNuevo;
        if (reservaActualizar.getFechaFinReserva() != null && reservaActualizar.getFechaReserva() != null) {
            long minutos = java.time.Duration.between(reservaActualizar.getFechaReserva(), reservaActualizar.getFechaFinReserva()).toMinutes();
            finNuevo = inicioNuevo.plusMinutes(Math.max(minutos, 30));
        } else {
            int duracionReservaMinutos = resolveDuracionReservaMinutos(reservaActualizar.getMesas());
            finNuevo = inicioNuevo.plusMinutes(duracionReservaMinutos);
        }
        validarCruceHorarios(reservaActualizar.getMesas(), inicioNuevo, finNuevo, reservaActualizar.getId());
        reservaActualizar.setCantidadPersonas(actualizarReservaDTO.cantidadPersonas());
        reservaActualizar.setFechaReserva(inicioNuevo);
        reservaActualizar.setFechaFinReserva(finNuevo);
        reservasRepo.actualizarReservaSimple(reservaActualizar);
        return "La reserva fue actualizada";
    }

    @Override
    public ReservaItemDTO obtenerInformacionReserva(String idReserva) throws Exception {
        Reserva reserva = obtenerReserva(idReserva);
        return mapearAReservaItemDTO(reserva);
    }

    private ReservaItemDTO mapearAReservaItemDTO(Reserva reserva) {
        return new ReservaItemDTO(
                reserva.getUsuarioId() != null ? reserva.getUsuarioId().toString() : null,
                reserva.getFechaReserva(),
                getFechaFinReservaEfectiva(reserva),
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
        return obtenerReservaItemDTO(reservas);
    }

    @Override
    public List<MesaHorarioReservadoDTO> listarHorariosReservadosMesa(String mesaId) throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservas = reservasRepo.filtrarReservasSimple(reserva -> {
            if (!isReservaBloqueante(reserva, ahora)) {
                return false;
            }
            LocalDateTime fin = getFechaFinReservaEfectiva(reserva);
            if (fin.isBefore(ahora)) {
                return false;
            }
            return reserva.getMesas() != null && reserva.getMesas().stream().anyMatch(mesa -> mesaId.equals(mesa.getId()));
        });

        return reservas.stream()
                .map(reserva -> new MesaHorarioReservadoDTO(reserva.getFechaReserva(), getFechaFinReservaEfectiva(reserva)))
                .sorted(Comparator.comparing(MesaHorarioReservadoDTO::inicio))
                .toList();
    }

    private void validarCruceHorarios(List<Mesa> mesas, LocalDateTime inicio, LocalDateTime fin, String reservaActualId) throws Exception {
        if (mesas == null || mesas.isEmpty()) {
            return;
        }
        for (Mesa mesa : mesas) {
            boolean existeCruce = hayCruceReservaMesa(mesa.getId(), inicio, fin, reservaActualId);
            if (existeCruce) {
                throw new IllegalStateException("La mesa " + mesa.getNombre() + " ya tiene una reserva en el horario seleccionado");
            }
        }
    }

    private boolean hayCruceReservaMesa(String mesaId, LocalDateTime inicio, LocalDateTime fin, String reservaActualId) throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasMesa = reservasRepo.filtrarReservasSimple(reserva -> {
            if (reservaActualId != null && reservaActualId.equals(reserva.getId())) {
                return false;
            }
            if (!isReservaBloqueante(reserva, ahora)) {
                return false;
            }
            return reserva.getMesas() != null && reserva.getMesas().stream().anyMatch(mesa -> mesaId.equals(mesa.getId()));
        });

        for (Reserva reserva : reservasMesa) {
            LocalDateTime inicioExistente = reserva.getFechaReserva();
            LocalDateTime finExistente = getFechaFinReservaEfectiva(reserva);
            boolean solapa = inicio.isBefore(finExistente) && fin.isAfter(inicioExistente);
            if (solapa) {
                return true;
            }
        }
        return false;
    }

    private boolean isReservaBloqueante(Reserva reserva, LocalDateTime referencia) {
        if (reserva.getEstadoReserva() == null) {
            return false;
        }
        if (EstadoReserva.CONFIRMADA.equals(reserva.getEstadoReserva())) {
            return true;
        }
        if (EstadoReserva.PENDIENTE.equals(reserva.getEstadoReserva())) {
            return isReservaPendienteActiva(reserva, referencia);
        }
        return false;
    }

    private boolean isReservaPendienteActiva(Reserva reserva, LocalDateTime referencia) {
        if (reserva.getFechaCreacion() == null) {
            return false;
        }
        LocalDateTime expiracion = reserva.getFechaCreacion().plusMinutes(pendingReservationHoldMinutes);
        return !referencia.isAfter(expiracion);
    }

    private LocalDateTime getFechaFinReservaEfectiva(Reserva reserva) {
        if (reserva.getFechaFinReserva() != null) {
            return reserva.getFechaFinReserva();
        }
        int duracionReservaMinutos = resolveDuracionReservaMinutos(reserva.getMesas());
        return reserva.getFechaReserva().plusMinutes(duracionReservaMinutos);
    }

    private int resolveDuracionReservaMinutos(List<Mesa> mesas) {
        if (mesas == null || mesas.isEmpty()) {
            return DURACION_RESERVA_MINUTOS_DEFAULT;
        }
        return mesas.stream()
                .mapToInt(mesa -> mesa != null ? mesa.getDuracionReservaMinutos() : 0)
                .filter(valor -> valor > 0)
                .max()
                .orElse(DURACION_RESERVA_MINUTOS_DEFAULT);
    }

    private List<ReservaItemDTO> obtenerReservaItemDTO(List<Reserva> reservas) {
        return reservas.stream().map(this::mapearAReservaItemDTO).collect(Collectors.toList());
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

                if (mercadoPagoAccessToken == null || mercadoPagoAccessToken.isBlank()) {
                throw new IllegalStateException("No se configuró MERCADOPAGO_ACCESS_TOKEN");
                }
                MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(buildFrontendBackUrl("success"))
                    .failure(buildFrontendBackUrl("failure"))
                    .pending(buildFrontendBackUrl("pending"))
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .backUrls(backUrls)
                    .items(itemsGateway)
                    .metadata(Map.of("id_reserva", reservaGuardar.getId()))
                    .notificationUrl(mercadoPagoWebhookBaseUrl + "/api/public/reserva/receive-notification")
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

    private String buildFrontendBackUrl(String status) {
        return mercadoPagoFrontendBaseUrl + "/?status=" + status;
    }

    private String sanitizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
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
