package co.uniquindio.tiendasana.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
        @NotBlank String category,
        @NotBlank String name,
        @NotBlank @JsonProperty("product") String product,
        @NotBlank String contact,
        @NotBlank String address,
        @NotBlank String city
) {
}
