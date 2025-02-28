package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.repos.TableRepo;
import co.uniquindio.tiendasana.services.interfaces.TableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TableServiceImp implements TableService {
    private final TableRepo tableRepo;

    public TableServiceImp(TableRepo tableRepo) {
        this.tableRepo = tableRepo;
    }
}