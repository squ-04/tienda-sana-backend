package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.mesadtos.ListaMesas;
import co.uniquindio.tiendasana.dto.mesadtos.MesaInfoDTO;
import co.uniquindio.tiendasana.model.documents.MesaDTO;

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
    ListaMesas obtenerMesasCliente(int pagina) throws Exception;

    /**
     * Metodo que obtiene una mesa por su id
     * @param mesaId id de la mesa
     * @return Mesa con la informacion de la mesa
     * @throws Exception
     */
    MesaDTO obtenerMesa(String mesaId) throws Exception;

    /**
     * Metodo que cambia el estado de una mesa
     * @param mesaId id de la mesa
     * @param estado nuevo estado de la mesa
     * @throws Exception
     */
    void cambiarEstadoMesa(String mesaId, String estado) throws Exception;
}
