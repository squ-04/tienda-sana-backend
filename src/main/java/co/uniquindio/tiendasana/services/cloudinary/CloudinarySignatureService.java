package co.uniquindio.tiendasana.services.cloudinary;

import co.uniquindio.tiendasana.config.CloudinaryProperties;
import co.uniquindio.tiendasana.dto.cloudinary.CloudinarySignatureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CloudinarySignatureService {

    private final CloudinaryProperties properties;

    public CloudinarySignatureResponse createSignature() {
        validateConfig();

        long timestamp = Instant.now().getEpochSecond();
        String toSign = "timestamp=" + timestamp + properties.getApiSecret();
        String signature = sha1Hex(toSign);

        return new CloudinarySignatureResponse(
                timestamp,
                signature,
                properties.getApiKey(),
                properties.getCloudName()
        );
    }

    private void validateConfig() {
        List<String> missing = new ArrayList<>();
        if (isBlank(properties.getCloudName())) {
            missing.add("CLOUDINARY_CLOUD_NAME");
        }
        if (isBlank(properties.getApiKey())) {
            missing.add("CLOUDINARY_API_KEY");
        }
        if (isBlank(properties.getApiSecret())) {
            missing.add("CLOUDINARY_API_SECRET");
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Cloudinary no esta configurado correctamente. Variables faltantes: " + String.join(", ", missing));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String sha1Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar la firma Cloudinary", e);
        }
    }
}
