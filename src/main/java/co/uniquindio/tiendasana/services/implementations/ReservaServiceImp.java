package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.reservadtos.ActualizarReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.CrearReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.PaymentResponseReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.ReservaItemDTO;
import co.uniquindio.tiendasana.model.documents.*;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import co.uniquindio.tiendasana.model.vo.DetalleVentaProducto;
import co.uniquindio.tiendasana.repos.ReservasRepo;
import co.uniquindio.tiendasana.services.interfaces.*;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setUsuarioId(crearReservaDTO.emailUsuario());

        Cuenta cuenta = cuentaService.obtenerCuentaPorEmail(crearReservaDTO.emailUsuario());

        reserva.setId(UUID.randomUUID().toString());

        GestorReservas gestorReservas = gestorReservasService.obtenerGestorReservas(crearReservaDTO.emailUsuario());
        List<Mesa> items = obtenerMesas(gestorReservas, reserva.getId());
        reserva.setMesas(items);


        reserva.setValorReserva(calcularTotal(items, crearReservaDTO.emailUsuario()));


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
                mesaReservada.setNombre(mesaAReservar.getNombre());
                mesaReservada.setEstado(EstadoMesa.fromEstado(mesaAReservar.getEstado()));
                mesaReservada.setLocalidad(mesaAReservar.getLocalidad());
                mesaReservada.setPrecioReserva(mesaAReservar.getPrecioReserva());
                mesaReservada.setImagen(mesaAReservar.getImagen());
                mesaReservada.setIdReserva(id);
                mesaReservada.setIdGestorReserva("-");
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
    public String cancelarReserva(String idReserva) {
        return "";
    }

    @Override
    public String actualizarReserva(ActualizarReservaDTO actualizarReservaDTO) {
        return "";
    }

    @Override
    public ReservaItemDTO obtenerInformacionReserva(String idReserva) {
        return null;
    }

    @Override
    public List<ReservaItemDTO> listarReservasCliente(String clienteId) {
        return List.of();
    }

    @Override
    public PaymentResponseReservaDTO procesarPagoReserva(String idReserva) throws Exception {
        return null;
    }

    @Override
    public void receiveNotificationFromMercadoPago(Map<String, Object> request) {

    }
}
