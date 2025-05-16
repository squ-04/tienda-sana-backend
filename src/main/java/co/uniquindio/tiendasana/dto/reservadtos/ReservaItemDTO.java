package co.uniquindio.tiendasana.dto.reservadtos;

import co.uniquindio.tiendasana.model.documents.Mesa;

import java.time.LocalDateTime;
import java.util.List;

public record ReservaItemDTO(
        String emailUsuario,
        LocalDateTime fechaReserva,
        String estadoReserva,
        String paymentType,
        String status,
        LocalDateTime paymentDate,
        float transactionValue,
        String id,
        float total,
        String cantidadPersonas,
        List<Mesa> mesas
) {
}
