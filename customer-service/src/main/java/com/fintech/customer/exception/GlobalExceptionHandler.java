package com.fintech.customer.exception;

import feign.FeignException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern MENSAJE_JSON = Pattern.compile("\"mensaje\"\\s*:\\s*\"(.*?)\"");

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(ClienteNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> duplicado(RecursoDuplicadoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorRespuesta> saldoInsuficiente(SaldoInsuficienteException ex) {
        return respuesta(HttpStatus.valueOf(422), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return respuesta(HttpStatus.BAD_REQUEST, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido. Revisá el formato JSON");
    }

    // Errores al llamar a product-service por Feign:
    // - si product-service respondió un 4xx (ej: 409 número duplicado), se propaga ese código y su mensaje
    // - si no respondió (caído o no registrado en Eureka), se responde 503
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorRespuesta> errorProductService(FeignException ex) {
        int status = ex.status();
        if (status >= 400 && status < 500) {
            return ResponseEntity.status(status)
                    .body(new ErrorRespuesta(status, "product-service: " + extraerMensaje(ex)));
        }
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de productos no está disponible en este momento. Intentá más tarde");
    }

    private String extraerMensaje(FeignException ex) {
        Matcher matcher = MENSAJE_JSON.matcher(ex.contentUTF8());
        return matcher.find() ? matcher.group(1) : "error " + ex.status();
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorRespuesta(status.value(), mensaje));
    }
}
