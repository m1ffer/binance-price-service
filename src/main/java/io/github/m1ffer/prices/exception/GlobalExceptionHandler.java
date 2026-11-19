package io.github.m1ffer.prices.exception;

import io.github.m1ffer.prices.controller.PriceController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = PriceController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(PriceNotFoundException.class)
    public ProblemDetail handlePriceNotFound(PriceNotFoundException e) {
        log.info(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledException(Exception e) {
        log.error("Unhandled internal server error occurred", e);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }
}
