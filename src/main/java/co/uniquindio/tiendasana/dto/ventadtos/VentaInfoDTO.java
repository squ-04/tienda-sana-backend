package co.uniquindio.tiendasana.dto.ventadtos;

import co.uniquindio.tiendasana.model.vo.DetalleVentaProducto;

import java.time.LocalDateTime;
import java.util.List;

public record VentaInfoDTO(
        String clienteId,
        LocalDateTime fecha,
        List<DetalleVentaProducto> items,
        String paymentType,
        String estado,
        LocalDateTime paymentDate,
        float transactionValue,
        String id,
        float total,
        String productoId
) {
}
