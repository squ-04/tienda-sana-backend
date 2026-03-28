package co.uniquindio.tiendasana.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupplierResponse(
        String id,
        String category,
        String name,
        @JsonProperty("product") String product,
        String contact,
        String address,
        String city,
        boolean active
) {
}
