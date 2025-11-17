package org.example.videoapi21.Response;


import java.time.LocalDateTime;

public record CustomErrorResponse(int statusCode, String message, LocalDateTime timeStamp, String path) {
    public CustomErrorResponse(int statusCode, String message, String path) {
        this(statusCode, message, LocalDateTime.now(), path);
    }
}
