package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.MesaDTO;
import co.uniquindio.tiendasana.dto.gestorReservasdtos.BorrarMesaGestorDTO;
import co.uniquindio.tiendasana.model.documents.GestorReservas;

import java.util.List;

public interface GestorReservasService {

    /**
     * Metodo para crear el gestor de reservas
     * @param emailUsuario
     * @return
     */
    GestorReservas crearGestorReservas (String emailUsuario);

    /**
     * Metodo que borra TODOS los elementos del gestor de reservas
     * @param emailUsuario
     * @return
     */
    String borrarGestorReservas (String emailUsuario);

    /**
     * Obtiene el gestor de reservas de usuario, si existe
     * @param emailUsuario
     * @return
     */
    GestorReservas obtenerGestorReservas (String emailUsuario);

    /**
     * Agrega una mesa al gestor de reservas, si el gestor de reservas no existe se crea uno
     * @param mesaDTO
     * @return
     */
    String agregarMesaGestorReservas (MesaDTO mesaDTO);

    /**
     * Elimina una mesa específica del gestor de reservas del usuario
     * @param mesaDTO
     * @return
     */
    String borrarMesaGestorReservas (BorrarMesaGestorDTO mesaBorrarDTO);

    /**
     * Lista todas las mesas dentro del gestor de reservas de usuario
     * @param emailUsuario
     * @return
     */
    List<MesaDTO> obtenerMesasGestorReservas(String emailUsuario);
}
