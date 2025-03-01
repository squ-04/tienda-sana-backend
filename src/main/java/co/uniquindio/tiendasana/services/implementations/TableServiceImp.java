package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.tableDTO;
import co.uniquindio.tiendasana.model.documents.Location;
import co.uniquindio.tiendasana.model.documents.Table;
import co.uniquindio.tiendasana.repos.LocationRepo;
import co.uniquindio.tiendasana.repos.TableRepo;
import co.uniquindio.tiendasana.services.interfaces.TableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TableServiceImp implements TableService {
    private final TableRepo tableRepo;
    private final LocationRepo locationRepo;

    public TableServiceImp(TableRepo tableRepo,LocationRepo locationRepo) {
        this.tableRepo = tableRepo;
        this.locationRepo = locationRepo;
    }

    @Override
    public void createUpdatePTable(tableDTO tableInfo) {
        Optional<Table> obtainedTable=tableRepo.findByName(tableInfo.name());
        Optional<Location> location=locationRepo.findByName(tableInfo.name());
        Table table=new Table(
                tableInfo.name(),
                tableInfo.status(),
                location.get().getId().toString()
        );
        if (obtainedTable.isPresent()) {
            table.setId(obtainedTable.get().getId());
        }
        tableRepo.save(table);
    }

    @Override
    public void deleteTable(tableDTO table) {
        Optional<Table> tableObtained=tableRepo.findByName(table.name());
        tableRepo.delete(tableObtained.get());
    }
}