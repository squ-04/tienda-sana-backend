package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.mesadtos.ListaMesas;
import co.uniquindio.tiendasana.dto.mesadtos.MesaInfoDTO;
import co.uniquindio.tiendasana.model.documents.MesaDTO;
import co.uniquindio.tiendasana.services.interfaces.MesaService;
import org.springframework.stereotype.Service;

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
    public MesaDTO obtenerMesa(String mesaId) throws Exception {
        return null;
    }

    @Override
    public void cambiarEstadoMesa(String mesaId, String estado) throws Exception {

    }
}