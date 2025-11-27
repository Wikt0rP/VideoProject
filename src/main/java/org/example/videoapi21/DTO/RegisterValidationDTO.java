package org.example.videoapi21.DTO;

public record RegisterValidationDTO(boolean isValid, String message) {
    public RegisterValidationDTO(boolean isValid){
        this(isValid, isValid ? "(AUTO) Password is valid" : "(AUTO) Password is invalid");
    }
}
