package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.tableDTO;
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

    @Override
    public void createUpdatePTable(tableDTO table) {

    }

    @Override
    public void deleteTable(tableDTO table) {

    }
/*
    @Override
    public void createUpdatePTable(tableDTO tableInfo) {
        Optional<Mesa> obtainedTable=tableRepo.findByName(tableInfo.name());
        Optional<Locations> location=locationRepo.findByName(tableInfo.name());
        Mesa table=new Mesa(
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
        Optional<Mesa> tableObtained=tableRepo.findByName(table.name());
        tableRepo.delete(tableObtained.get());
    }*/
}