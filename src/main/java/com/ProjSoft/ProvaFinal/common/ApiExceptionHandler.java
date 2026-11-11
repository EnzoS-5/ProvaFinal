package com.ProjSoft.ProvaFinal.common;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
  public record ApiError(
      Instant timestamp, int status, String message, String path, Map<String, String> fields) {}

  private ResponseEntity<ApiError> response(
      int status, String message, HttpServletRequest req, Map<String, String> fields) {
    return ResponseEntity.status(status)
        .body(new ApiError(Instant.now(), status, message, req.getRequestURI(), fields));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    var fields =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    e -> e.getField(), e -> String.valueOf(e.getDefaultMessage()), (a, b) -> a));
    return response(400, "Confira os campos informados.", req, fields);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ApiError> malformed(Exception ex, HttpServletRequest req) {
    return response(400, "JSON, enum, identificador ou campo inválido.", req, Map.of());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiError> invalid(IllegalArgumentException ex, HttpServletRequest req) {
    return response(400, ex.getMessage(), req, Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> forbidden(AccessDeniedException ex, HttpServletRequest req) {
    return response(403, "Acesso não permitido.", req, Map.of());
  }
}
