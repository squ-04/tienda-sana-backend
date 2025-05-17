package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.mesadtos.FiltroMesaDTO;
import co.uniquindio.tiendasana.dto.mesadtos.ListaMesasDTO;
import co.uniquindio.tiendasana.dto.mesadtos.MesaInfoDTO;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.Localidad;

import java.util.List;

public interface MesaService {

    /**
     * Metodo que obtiene la informacion de una mesa
     * @param mesaId id de la mesa
     * @return MesaInfoDTO con la informacion de la mesa
     * @throws Exception
     */
    MesaInfoDTO obtenerInfoMesa(String mesaId) throws Exception;

    /**
     * Metodo que obtiene la lista de mesas de un cliente
     * @param pagina pagina a obtener
     * @return ListaMesas con la lista de mesas
     * @throws Exception
     */
    ListaMesasDTO obtenerMesasCliente(int pagina) throws Exception;

    /**
     * Metodo que obtiene una mesa por su id
     * @param mesaId id de la mesa
     * @return Mesa con la informacion de la mesa
     * @throws Exception
     */
    Mesa obtenerMesa(String mesaId) throws Exception;

    /**
     * Metodo que cambia el estado de una mesa
     * @param mesaId id de la mesa
     * @param estado nuevo estado de la mesa
     * @throws Exception
     */
    void cambiarEstadoMesa(String mesaId, String estado) throws Exception;

    ListaMesasDTO filtrarMesas(FiltroMesaDTO filtroMesaDTO) throws Exception;

    List<Localidad> listarLocalidades() throws Exception;
}
