package org.example.videoapi21.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.example.videoapi21.Entity.AppUser;
import org.example.videoapi21.Exception.UserNotFoundException;
import org.example.videoapi21.Repository.AppUserRepository;
import org.example.videoapi21.Security.JwtUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public AppUser getUserFromRequest(HttpServletRequest request) throws UserNotFoundException{
        String token = JwtUtils.getJwtFromRequest(request);
        return getUserFromToken(token);
    }


    public AppUser getUserFromToken(String token) throws UserNotFoundException{
        String username = jwtUtils.extractUsername(token);
        if (username == null || username.isEmpty()) {
            return null;
        }
        return userRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("User nof found"));
    }
}
