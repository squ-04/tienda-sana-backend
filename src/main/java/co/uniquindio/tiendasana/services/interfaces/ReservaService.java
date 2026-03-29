package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.reservadtos.ActualizarReservaDTO;
import co.uniquindio.tiendasana.dto.mesadtos.MesaHorarioReservadoDTO;
import co.uniquindio.tiendasana.dto.reservadtos.CrearReservaDirectaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.CrearReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.PaymentResponseReservaDTO;
import co.uniquindio.tiendasana.dto.reservadtos.ReservaItemDTO;
import co.uniquindio.tiendasana.model.documents.Reserva;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ReservaService {
    /**
     * Realiza una reserva de mesa en el restaurante.
     *
     * @param crearReservaDTO Objeto que contiene la información de la reserva.
     * @return Un mensaje indicando el resultado de la reserva.
     */
    String reservarMesa(CrearReservaDTO crearReservaDTO) throws Exception;

    /**
     * Crea una reserva directa desde el detalle de mesa sin pasar por el gestor de reservas.
     */
    String reservarMesaDirecta(CrearReservaDirectaDTO crearReservaDirectaDTO) throws Exception;

    /**
     * Obtiene la información de una reserva por su ID.
     * @param idReserva ID de la reserva a buscar.
     * @return Objeto Reserva con la información de la reserva.
     */
    Reserva obtenerReserva(String idReserva) throws Exception;

    /**
     * Metodo para borrar una reserva
     * @param idReserva ID de la reserva a borrar
     * @return Mensaje indicando el resultado de la operación.
     */
    String cancelarReserva(String idReserva) throws Exception;

    /**
     * Actualiza la información de una reserva existente.
     *
     * @param actualizarReservaDTO Objeto que contiene la información actualizada de la reserva.
     * @return Un mensaje indicando el resultado de la actualización.
     */
    String actualizarReserva(ActualizarReservaDTO actualizarReservaDTO) throws Exception;

    /**
     * Obtiene la información de una reserva por su ID.
     *
     * @param idReserva ID de la reserva a buscar.
     * @return Objeto ReservaItemDTO con la información de la reserva.
     */
    ReservaItemDTO obtenerInformacionReserva(String idReserva) throws Exception;

    /**
     * Lista todas las reservas de un cliente.
     *
     * @param emailUsuario email del cliente cuyas reservas se desean listar.
     * @return Lista de objetos ReservaItemDTO con la información de las reservas del cliente.
     */
    List<ReservaItemDTO> listarReservasCliente(String emailUsuario) throws IOException;

    /**
     * Retorna franjas de tiempo reservadas para una mesa sin exponer datos personales.
     */
    List<MesaHorarioReservadoDTO> listarHorariosReservadosMesa(String mesaId) throws Exception;

    /**
     * Realiza el pago de una reserva.
     * @param idReserva ID de la reserva a pagar.
     * @return Objeto PaymentResponseReservaDTO con la información del pago.
     * @throws Exception en caso de que ocurra un error durante el proceso de pago.
     */
    PaymentResponseReservaDTO procesarPagoReserva(String idReserva) throws Exception;

    /**
     * Enviar resumen de compra por correo electrónico
     * @return ID de la venta
     * @throws Exception
     */
    void receiveNotificationFromMercadoPago(Map<String, Object> request);
}
