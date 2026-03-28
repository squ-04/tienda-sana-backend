package co.uniquindio.tiendasana.dto.admin;

import co.uniquindio.tiendasana.model.enums.TableStatus;
import jakarta.validation.constraints.NotNull;

public record TableStatusPatchRequest(
        @NotNull TableStatus status
) {
}
