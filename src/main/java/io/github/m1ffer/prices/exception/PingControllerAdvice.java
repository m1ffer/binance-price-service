package io.github.m1ffer.prices.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PingControllerAdvice {
    @ExceptionHandler(NoPingException.class)
    public String handleNoPing(){
        return "No pings yet";
    }
    @ExceptionHandler(Exception.class)
    public String handleAll(Exception e){
        return "Something went wrong: " + e.getMessage();
    }
}
