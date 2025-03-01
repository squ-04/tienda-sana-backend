package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.locationDTO;
import jakarta.validation.Valid;

public interface LocationService {
    void createUpdateLocation(@Valid locationDTO location);

    void deleteLocation(@Valid locationDTO location);
}
