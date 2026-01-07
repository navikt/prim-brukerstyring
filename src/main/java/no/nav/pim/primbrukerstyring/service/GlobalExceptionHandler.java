package no.nav.pim.primbrukerstyring.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import no.nav.pim.primbrukerstyring.exceptions.AuthorizationException;
import no.nav.pim.primbrukerstyring.exceptions.ForbiddenException;
import no.nav.pim.primbrukerstyring.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<CustomExceptionResponse> handleForbiddenException(ForbiddenException fex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                CustomExceptionResponse.builder()
                        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                        .message(fex.getMessage())
                        .path(request.getRequestURI())
                        .status(HttpStatus.FORBIDDEN.value())
                        .timestamp(System.currentTimeMillis())
                        .build()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomExceptionResponse> handleForbiddenException(NotFoundException fex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                CustomExceptionResponse.builder()
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(fex.getMessage())
                        .path(request.getRequestURI())
                        .status(HttpStatus.NOT_FOUND.value())
                        .timestamp(System.currentTimeMillis())
                        .build()
        );
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<CustomExceptionResponse> handleForbiddenException(AuthorizationException fex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                CustomExceptionResponse.builder()
                        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                        .message(fex.getMessage())
                        .path(request.getRequestURI())
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .timestamp(System.currentTimeMillis())
                        .build()
        );
    }

    @Builder(toBuilder = true)
    public static class CustomExceptionResponse {
        String error;
        String message;
        String path;
        int status;
        long timestamp;
    }

}
