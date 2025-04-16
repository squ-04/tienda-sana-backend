package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;

import java.util.List;

public interface CarritoComprasService {


    void borrarCarritoCompras(String idUsuario);

    List<DetalleCarrito> getItems();

    CarritoCompras getCarritoCompras(String idUsuario);
}
