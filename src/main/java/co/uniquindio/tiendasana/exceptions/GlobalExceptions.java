package co.uniquindio.tiendasana.exceptions;

import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptions {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageDTO<String>> generalException(Exception e) {
        return ResponseEntity.internalServerError().body(new MessageDTO<>(true, e.getMessage())
        );
    }

    @ExceptionHandler(ProductoParseException.class)
    public ResponseEntity<MessageDTO<String>> handleProductParseException(ProductoParseException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO<>(true, e.getMessage()));
    }
}
