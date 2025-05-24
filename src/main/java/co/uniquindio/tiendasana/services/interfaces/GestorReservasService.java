package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.MesaDTO;
import co.uniquindio.tiendasana.dto.gestorReservasdtos.BorrarMesaGestorDTO;
import co.uniquindio.tiendasana.model.documents.GestorReservas;

import java.io.IOException;
import java.util.List;

public interface GestorReservasService {

    /**
     * Metodo para crear el gestor de reservas
     * @param emailUsuario Email del usuario dueño del gestor de reservas
     * @return Id del gestor de reservas
     * @throws IOException Error al acceder a la base de datos
     */
    GestorReservas crearGestorReservas (String emailUsuario) throws IOException;

    /**
     * Metodo que borra TODOS los elementos del gestor de reservas
     * @param emailUsuario Email del usuario dueño del gestor de reservas
     * @return Id del gestor de reservas
     * @throws IOException Error al acceder a la base de datos
     */
    String borrarGestorReservas (String emailUsuario) throws IOException;

    /**
     * Obtiene el gestor de reservas de usuario, si existe
     * @param emailUsuario Email del usuario de la cuenta
     * @return Gestor de reservas encontrado
     * @throws IOException Error al acceder a la base de datos
     */
    GestorReservas obtenerGestorReservas (String emailUsuario) throws IOException;

    /**
     * Agrega una mesa al gestor de reservas, si el gestor de reservas no existe se crea uno
     * @param mesaDTO Datos de la mesa
     * @return Id de la mesa agregada
     * @throws IOException Error al acceder a la base de datos
     */
    String agregarMesaGestorReservas (MesaDTO mesaDTO) throws IOException;

    /**
     * Elimina una mesa específica del gestor de reservas del usuario
     * @param mesaBorrarDTO Datos de la mesa a eliminar
     * @return Id de la mesa
     * @throws IOException Error al acceder a la base de datos
     */
    String borrarMesaGestorReservas (BorrarMesaGestorDTO mesaBorrarDTO) throws IOException;

    /**
     * Lista todas las mesas dentro del gestor de reservas de usuario
     * @param emailUsuario Email del usuario al que pertenece el gestor de reservas
     * @return Mesas respectivas del gestor de reservas del usuario
     * @throws IOException Error al acceder a la base de datos
     */
    List<MesaDTO> obtenerMesasGestorReservas(String emailUsuario) throws IOException;
}
