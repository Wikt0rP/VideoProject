package org.example.videoapi21.Exception.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import org.example.videoapi21.Exception.CouldNotSaveFileException;
import org.example.videoapi21.Exception.InvalidImageFormatException;
import org.example.videoapi21.Response.CustomErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ThumbnailsExcpetionHandler {

    @ExceptionHandler(value = InvalidImageFormatException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public CustomErrorResponse handleInvalidImageFormatException(InvalidImageFormatException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(value = CouldNotSaveFileException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public CustomErrorResponse handleCouldNotSaveFileException(CouldNotSaveFileException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), request.getRequestURI());
    }
}
