package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.carritoCompras.AgregarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.BorrarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.EditarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.VistaItemCarritoDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

public interface CarritoComprasService {


    void borrarCarritoCompras(String idUsuario);

    List<DetalleCarrito> getItems();

    CarritoCompras getCarritoCompras(String idUsuario);

    String agregarDetalleCarrito(@Valid AgregarDetalleCarritoDTO addShoppingCarDetailDTO) throws IOException, ProductoParseException;

    String editarDetalleCarrito(@Valid EditarDetalleCarritoDTO editCarDetailDTO) throws Exception;

    String borrarCarritoCompras(@Valid BorrarDetalleCarritoDTO deleteCarDetailDTO) throws Exception;

    List<VistaItemCarritoDTO> listarDetallesCarrito(String emailUsuario) throws IOException;

    CarritoCompras crearCarritoCompras(String idUsuario) throws IOException;

}
