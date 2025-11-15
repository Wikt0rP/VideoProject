package org.example.videoapi21.Response;

import org.example.videoapi21.Entity.AppUser;

public record UserValidationResponse(String status, AppUser user) {
}
