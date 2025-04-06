package co.uniquindio.tiendasana.dto.jwtdtos;

/**
 * DTO usado para enviar mensajes en las solicitudes HTTP
 * @param error
 * @param reply
 * @param <T>
 */
public record MessageDTO<T>(
        boolean error,
        T reply
) {
}
