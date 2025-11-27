package org.example.videoapi21.Exception.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.example.videoapi21.Exception.UnableToSetResourcePath;
import org.example.videoapi21.Exception.UserNotFoundException;
import org.example.videoapi21.Response.CustomErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomErrorResponse handleIOException(IOException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(value = UnableToSetResourcePath.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomErrorResponse handleUnableToSetResourcePathException(UnableToSetResourcePath ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public CustomErrorResponse handleAccessDeniedExcepion(AccessDeniedException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomErrorResponse handleUserNotFound(UserNotFoundException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
    }
}
