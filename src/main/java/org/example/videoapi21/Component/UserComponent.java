package org.example.videoapi21.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.example.videoapi21.Entity.AppUser;
import org.example.videoapi21.Repository.AppUserRepository;
import org.example.videoapi21.Response.UserValidationResponse;
import org.example.videoapi21.Security.JwtUtils;
import org.springframework.stereotype.Component;

@Component
public class UserComponent {

    private final JwtUtils jwtUtils;
    private final AppUserRepository userRepository;

    public UserComponent(JwtUtils jwtUtils, AppUserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    /**
     * Get user from request
     * @return HashMap<String, User> where you can find values: <br>
     * "Can not validate user"<br>
     * "Cannot find user"<br>
     * "User is not active" + User<br>
     * "OK" + User<br>
     */
    public UserValidationResponse getUserFromRequest(HttpServletRequest request){
        String token = JwtUtils.getJwtFromRequest(request);

        if(token == null || !jwtUtils.validateToken(token)){
            return new UserValidationResponse("Can not validate user", null);
        }

        AppUser user = getUserFromToken(token);
        if(user == null){
            return new UserValidationResponse("Cannot find user", null);
        }

        return new UserValidationResponse("OK", user);
    }


    public AppUser getUserFromToken(String token){
        String username = jwtUtils.extractUsername(token);
        if (username == null || username.isEmpty()) {
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }
}
