package co.uniquindio.tiendasana.dto.ventadtos;

import java.time.LocalDateTime;
import java.util.List;

public record VentaItemDTO (
        String clienteId,
        LocalDateTime fecha,
        List<DetalleOrdenDTO> productos,
        String tipoPago,
        String estado,
        LocalDateTime fechaPago,
        float valorTranasccion,//?
        String id,
        float total,
        String promocionId

){
}
