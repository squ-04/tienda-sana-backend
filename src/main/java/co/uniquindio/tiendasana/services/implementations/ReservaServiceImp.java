package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.reservadtos.ActualizarReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.CrearReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.PaymentResponseReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.ReservaItemDTO;
import co.uniquindio.tiendasana.model.documents.Reserva;
import co.uniquindio.tiendasana.services.interfaces.ReservaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReservaServiceImp implements ReservaService {
    @Override
    public String reservarMesa(CrearReservaDTO crearReservaDTO) {
        return "";
    }

    @Override
    public Reserva obtenerReserva(String idReserva) {
        return null;
    }

    @Override
    public String borrarReserva(String idReserva) {
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
