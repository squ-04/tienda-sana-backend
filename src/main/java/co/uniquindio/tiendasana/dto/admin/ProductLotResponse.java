package co.uniquindio.tiendasana.dto.admin;

import java.time.LocalDate;

public record ProductLotResponse(
        String id,
        String productId,
        String supplierId,
        LocalDate entryDate,
        int quantity,
        double unitValue
) {
}
