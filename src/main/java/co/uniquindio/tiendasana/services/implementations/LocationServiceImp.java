package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.locationDTO;
import co.uniquindio.tiendasana.repos.LocationRepo;
import co.uniquindio.tiendasana.services.interfaces.LocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class LocationServiceImp implements LocationService {
    private final LocationRepo locationRepo;

    public LocationServiceImp(LocationRepo locationRepo) {
        this.locationRepo = locationRepo;
    }

    @Override
    public void createUpdateLocation(locationDTO location) {

    }

    @Override
    public void deleteLocation(locationDTO location) {

    }
}