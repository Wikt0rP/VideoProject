package org.example.videoapi21.Exception.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import org.example.videoapi21.Exception.InvalidVideoFormatException;
import org.example.videoapi21.Exception.SendVideoTaskException;
import org.example.videoapi21.Exception.VideoNotFoundException;
import org.example.videoapi21.Response.CustomErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class VideoExcpetionHandler {
    @ExceptionHandler(value = SendVideoTaskException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomErrorResponse handleSendVideoTaskException(SendVideoTaskException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(value = InvalidVideoFormatException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public CustomErrorResponse handleInvalidVideoFormatException(InvalidVideoFormatException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(value = VideoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomErrorResponse handleVideoNotFoundException(VideoNotFoundException ex, HttpServletRequest request){
        return new CustomErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
    }
}
