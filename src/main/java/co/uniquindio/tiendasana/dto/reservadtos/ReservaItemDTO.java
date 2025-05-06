package co.uniquindio.tiendasana.dto.reservadtos;

import java.time.LocalDateTime;

public record ReservaItemDTO(
        String emailUsuario,
        LocalDateTime fechaReserva,
        String paymentType,
        String estadoReserva,
        LocalDateTime paymentDate,
        float transactionValue,
        String id,
        float total,
        String mesaId,
        String cantidadPersonas
) {
}
