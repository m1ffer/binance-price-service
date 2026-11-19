package io.github.m1ffer.prices.exception;

import io.github.m1ffer.prices.controller.PriceController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = PriceController.class)
public class ValidationExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<String> errors = ex.getAllErrors().stream()
                .map(error -> {
                    if ("AssertTrue".equals(error.getCode())) {
                        return error.getDefaultMessage();
                    }
                    if (error instanceof FieldError fieldError) {
                        if (fieldError.isBindingFailure()) {
                            Class<?> fieldType = ex.getBindingResult().getFieldType(fieldError.getField());
                            return formatTypeMismatchMessage(fieldError.getField(), fieldType, fieldError.getRejectedValue());
                        }
                        return formatErrorMessage(fieldError.getField(), fieldError.getDefaultMessage());
                    }
                    return error.getDefaultMessage();
                })
                .toList();

        return buildValidationResponse(errors, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<String> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> {
                    String paramName = result.getMethodParameter().getParameterName();
                    return result.getResolvableErrors().stream()
                            .map(error -> formatErrorMessage(paramName, error.getDefaultMessage()));
                })
                .toList();

        return buildValidationResponse(errors, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String message;
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            message = formatTypeMismatchMessage(
                    mismatch.getName(),
                    mismatch.getRequiredType(),
                    mismatch.getValue()
            );
        } else {
            message = ex.getMessage();
        }

        return buildValidationResponse(List.of(message), headers, status);
    }

    private String formatTypeMismatchMessage(String paramName, Class<?> requiredType, Object rejectedValue) {
        if (requiredType != null && Instant.class.isAssignableFrom(requiredType)) {
            return "Parameter '%s' must be a valid date in ISO-8601 format (got '%s')"
                    .formatted(paramName, rejectedValue);
        }
        if (requiredType != null && requiredType.isEnum()) {
            String allowedValues = Arrays.toString(requiredType.getEnumConstants());
            return "Parameter '%s' has invalid value '%s'. Expected one of: %s"
                    .formatted(paramName, rejectedValue, allowedValues);
        }
        if (requiredType != null) {
            return "Parameter '%s' must be of type %s (got '%s')"
                    .formatted(paramName, requiredType.getSimpleName(), rejectedValue);
        }
        return "Parameter '%s' has invalid value '%s'".formatted(paramName, rejectedValue);
    }

    private String formatErrorMessage(String paramName, String message) {
        if (message != null && message.startsWith("Parameter '")) {
            return message;
        }
        return "Parameter '%s' %s".formatted(paramName, message);
    }

    private ResponseEntity<Object> buildValidationResponse(
            List<String> errors,
            HttpHeaders headers,
            HttpStatusCode status
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", errors);
        log.debug("Validation failed for request: {}", errors);
        return ResponseEntity.status(status).headers(headers).body(problem);
    }
}
