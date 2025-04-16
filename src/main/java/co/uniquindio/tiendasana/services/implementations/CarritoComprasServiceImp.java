package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import co.uniquindio.tiendasana.services.interfaces.CarritoComprasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CarritoComprasServiceImp implements CarritoComprasService {
    @Override
    public void borrarCarritoCompras(String idUsuario) {

    }

    @Override
    public List<DetalleCarrito> getItems() {
        return List.of();
    }

    @Override
    public CarritoCompras getCarritoCompras(String idUsuario) {
        return null;
    }
}
