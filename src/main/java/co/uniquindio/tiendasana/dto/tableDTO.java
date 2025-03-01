package co.uniquindio.tiendasana.dto;

import co.uniquindio.tiendasana.model.enums.TableStatus;

public record tableDTO(
        String name,
        TableStatus status,
        String locationName
) {
}
