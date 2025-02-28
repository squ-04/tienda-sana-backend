package co.uniquindio.tiendasana.controllers;

import co.uniquindio.tiendasana.dto.MessageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> a(@Valid @RequestBody MessageDTO dto){

        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> ab(@Valid @RequestBody MessageDTO dto){

        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> ac(@Valid @RequestBody MessageDTO dto){

        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

    @PostMapping("/")
    public ResponseEntity<MessageDTO<String>> ae(@Valid @RequestBody MessageDTO dto){

        return ResponseEntity.ok(new MessageDTO<>(false,"prueba"));
    }

}
