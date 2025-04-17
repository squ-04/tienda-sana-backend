package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.carritoCompras.AgregarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.BorrarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.EditarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.VistaItemCarritoDTO;
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

    @Override
    public String agregarDetalleCarrito(AgregarDetalleCarritoDTO addShoppingCarDetailDTO) {
        return "";
    }

    @Override
    public String editarDetalleCarrito(EditarDetalleCarritoDTO editCarDetailDTO) {
        return "";
    }

    @Override
    public String borrarCarritoCompras(BorrarDetalleCarritoDTO deleteCarDetailDTO) {
        return "";
    }

    @Override
    public List<VistaItemCarritoDTO> listarDetallesCarrito(String emailUsuario) {
        return List.of();
    }
}
