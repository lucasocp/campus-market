package br.edu.campusmarket.common;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<?> business(BusinessException ex) { return error(ex.status(), ex.getMessage()); }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<?> credentials() { return error(401, "R.A. ou senha incorretos."); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage()).findFirst().orElse("Dados inválidos.");
        return error(400, message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HandlerMethodValidationException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> malformed(Exception ex) { return error(400, "Verifique o formato dos dados enviados."); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> duplicate() { return error(409, "A operação conflita com um registro existente. Atualize a página."); }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> invalidArgument() { return error(400, "Verifique os parâmetros enviados."); }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<?> tooLarge() { return error(413, "A imagem deve ter no máximo 5 MB."); }

    private ResponseEntity<?> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
