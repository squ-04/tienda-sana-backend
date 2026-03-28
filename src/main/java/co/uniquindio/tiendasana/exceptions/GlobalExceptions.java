package co.uniquindio.tiendasana.exceptions;

import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Clase donde se manejaran las diferentes excepciones generadas
 * en el proyecto
 */
@RestControllerAdvice
public class GlobalExceptions {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageDTO<String>> illegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageDTO<>(true, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageDTO<String>> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Datos de entrada inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageDTO<>(true, msg));
    }

    /**
     * Handler para las excepciones que sean de tipo Exception
     * Este manejara todas las excepciones que hereden de este tipo
     * y que su manejo no sea especificado
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageDTO<String>> generalException(Exception e, HttpServletRequest request) {
        return ResponseEntity.internalServerError().body(new MessageDTO<>(true, e.getMessage())
        );
    }

    /**
     * Handler para la excepcion de ProductoParseException especificamente
     * @param e
     * @return
     */
    @ExceptionHandler(ProductoParseException.class)
    public ResponseEntity<MessageDTO<String>> handleProductParseException(ProductoParseException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO<>(true, e.getMessage()));
    }
}
