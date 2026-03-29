package co.uniquindio.tiendasana.dto.reservadtos;

import co.uniquindio.tiendasana.model.documents.Mesa;

import java.time.LocalDateTime;
import java.util.List;

public record ReservaItemDTO(
        String emailUsuario,
        LocalDateTime fechaReserva,
        LocalDateTime fechaFinReserva,
        String estadoReserva,
        String paymentType,
        String status,
        LocalDateTime paymentDate,
        float transactionValue,
        String idReserva,
        float total,
        int cantidadPersonas,
        List<Mesa> mesas
) {
}
