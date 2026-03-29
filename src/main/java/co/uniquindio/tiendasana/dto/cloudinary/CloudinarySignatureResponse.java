package co.uniquindio.tiendasana.dto.cloudinary;

public record CloudinarySignatureResponse(
        long timestamp,
        String signature,
        String api_key,
        String cloud_name
) {
}
