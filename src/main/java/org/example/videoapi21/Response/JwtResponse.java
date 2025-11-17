package org.example.videoapi21.Response;

import org.example.videoapi21.Entity.Role;

import java.util.Set;

public record JwtResponse(
        String token,
        String type,
        Long id,
        String username,
        Set<Role> roles
) {
    public JwtResponse(String token, Long id, String username, Set<Role> roles) {
        this(token, "Bearer", id, username, roles);
    }
}

