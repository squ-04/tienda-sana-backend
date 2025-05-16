package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.mesadtos.FiltroMesaDTO;
import co.uniquindio.tiendasana.dto.mesadtos.ListaMesas;
import co.uniquindio.tiendasana.dto.mesadtos.MesaInfoDTO;
import co.uniquindio.tiendasana.dto.mesadtos.MesaItemDTO;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.services.interfaces.MesaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MesaServiceImp implements MesaService {

    @Override
    public MesaInfoDTO obtenerInfoMesa(String mesaId) throws Exception {
        return null;
    }

    @Override
    public ListaMesas obtenerMesasCliente(int pagina) throws Exception {
        return null;
    }

    @Override
    public Mesa obtenerMesa(String mesaId) throws Exception {
        return null;
    }

    @Override
    public void cambiarEstadoMesa(String mesaId, String estado) throws Exception {

    }

    @Override
    public List<MesaItemDTO> filtrarMesas(FiltroMesaDTO filtroMesaDTO) throws Exception {
        return List.of();
    }
}