package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.services.interfaces.MesaService;
import org.springframework.stereotype.Service;

@Service
public class MesaServiceImp implements MesaService {

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