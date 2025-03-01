package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.tableDTO;
import jakarta.validation.Valid;

public interface TableService {
    void createUpdatePTable(@Valid tableDTO table);

    void deleteTable(@Valid tableDTO table);
}
