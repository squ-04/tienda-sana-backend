package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.documents.Promocion;

public interface PromocionService {
    Promocion getPromocion(String idPromocion);
}
