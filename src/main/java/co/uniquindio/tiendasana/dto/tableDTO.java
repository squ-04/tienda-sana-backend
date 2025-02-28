package co.uniquindio.tiendasana.dto;

import co.uniquindio.tiendasana.model.enums.TableStatus;
import org.bson.types.ObjectId;

public record tableDTO(
        String name,
        TableStatus status,
        String locationId
) {
}
