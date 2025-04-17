package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.carritoCompras.AgregarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.BorrarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.EditarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.VistaItemCarritoDTO;
import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import jakarta.validation.Valid;

import java.util.List;

public interface CarritoComprasService {


    void borrarCarritoCompras(String idUsuario);

    List<DetalleCarrito> getItems();

    CarritoCompras getCarritoCompras(String idUsuario);

    String agregarDetalleCarrito(@Valid AgregarDetalleCarritoDTO addShoppingCarDetailDTO);

    String editarDetalleCarrito(@Valid EditarDetalleCarritoDTO editCarDetailDTO);

    String borrarCarritoCompras(@Valid BorrarDetalleCarritoDTO deleteCarDetailDTO);

    List<VistaItemCarritoDTO> listarDetallesCarrito(String emailUsuario);
}
