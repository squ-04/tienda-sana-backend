package co.uniquindio.tiendasana.dto.admin;

import co.uniquindio.tiendasana.model.enums.TableStatus;

public record RestaurantTableResponse(
        String id,
        int capacity,
        String location,
        boolean active,
        TableStatus status
) {
}
