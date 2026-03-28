package co.uniquindio.tiendasana.dto.admin;

public record AdminProductResponse(
        String id,
        String name,
        String description,
        String category,
        double price,
        String imageUrl,
        int stockQuantity,
        boolean active,
        boolean outOfStock
) {
}
