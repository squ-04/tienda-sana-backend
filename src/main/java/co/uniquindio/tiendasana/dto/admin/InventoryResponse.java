package co.uniquindio.tiendasana.dto.admin;

public record InventoryResponse(
        String productId,
        String productName,
        int stockQuantity
) {
}
