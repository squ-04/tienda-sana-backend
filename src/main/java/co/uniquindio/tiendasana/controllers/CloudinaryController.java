package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.cloudinary.CloudinarySignatureResponse;
import co.uniquindio.tiendasana.services.cloudinary.CloudinarySignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/cloudinary", "/api/admin/cloudinary"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CloudinaryController {

    private final CloudinarySignatureService cloudinarySignatureService;

    @GetMapping("/signature")
    public CloudinarySignatureResponse signature() {
        return cloudinarySignatureService.createSignature();
    }
}
