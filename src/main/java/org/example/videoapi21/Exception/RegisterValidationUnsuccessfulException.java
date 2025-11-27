package org.example.videoapi21.Exception;

public class RegisterValidationUnsuccessfulException extends RuntimeException {
    public RegisterValidationUnsuccessfulException(String message, String validationFailed) {
        super(message);
    }
}
