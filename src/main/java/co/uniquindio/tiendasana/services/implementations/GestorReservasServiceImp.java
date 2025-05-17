package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.MesaDTO;
import co.uniquindio.tiendasana.dto.gestorReservasdtos.BorrarMesaGestorDTO;
import co.uniquindio.tiendasana.model.documents.GestorReservas;
import co.uniquindio.tiendasana.services.interfaces.GestorReservasService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestorReservasServiceImp implements GestorReservasService {
    @Override
    public GestorReservas crearGestorReservas(String emailUsuario) {
        return null;
    }

    @Override
    public String borrarGestorReservas(String emailUsuario) {
        return "";
    }

    @Override
    public GestorReservas obtenerGestorReservas(String emailUsuario) {
        return null;
    }

    @Override
    public String agregarMesaGestorReservas(MesaDTO mesaDTO) {
        return "";
    }

    @Override
    public String borrarMesaGestorReservas(BorrarMesaGestorDTO mesaBorrarDTO) {
        return "";
    }

    @Override
    public List<MesaDTO> obtenerMesasGestorReservas(String emailUsuario) {
        return List.of();
    }
}
