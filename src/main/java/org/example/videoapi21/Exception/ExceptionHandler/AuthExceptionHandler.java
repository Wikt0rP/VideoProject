package org.example.videoapi21.Exception.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import org.example.videoapi21.Exception.RegisterValidationUnsuccessfulException;
import org.example.videoapi21.Response.CustomErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    //register
    @ExceptionHandler(value = RegisterValidationUnsuccessfulException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomErrorResponse handleRegisterValidationUnsuccessfulException(RegisterValidationUnsuccessfulException ex, HttpServletRequest request) {
        return new CustomErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getRequestURI());
    }

    // login
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CustomErrorResponse handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return new CustomErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), request.getRequestURI());
    }
}
