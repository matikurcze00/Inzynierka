package com.example.inzynierka.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(WrongDataException.class)
    ResponseEntity<ErrorInfo> handleWrongDataException(HttpServletRequest req, Exception ex) {
        return genErrorInfoResp(req, null, HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
    }

    @ExceptionHandler(NoSuchAlgorithmException.class)
    ResponseEntity<ErrorInfo> handleNoSuchAlgorithmException(HttpServletRequest req, Exception ex) {
        return genErrorInfoResp(req, null, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    ResponseEntity<ErrorInfo> genErrorInfoResp(HttpServletRequest req,
                                               String message,
                                               HttpStatus statusCode,
                                               String exception) {
        final ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setUrl(req.getRequestURI());
        errorInfo.setMethod(req.getMethod());
        errorInfo.setMessage(message);
        errorInfo.setException(exception);
        return new ResponseEntity<>(errorInfo, statusCode);
    }
}
