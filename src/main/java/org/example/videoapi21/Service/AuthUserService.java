package org.example.videoapi21.Service;

import org.example.videoapi21.Component.PasswordValidation;
import org.example.videoapi21.Component.UserComponent;
import org.example.videoapi21.Entity.AppUser;
import org.example.videoapi21.Entity.Role;
import org.example.videoapi21.Enum.ERole;
import org.example.videoapi21.Exception.RegisterValidationUnsuccessfulException;
import org.example.videoapi21.Repository.AppUserRepository;
import org.example.videoapi21.Repository.RoleRepository;
import org.example.videoapi21.Request.LoginRequest;
import org.example.videoapi21.Request.RegisterRequest;
import org.example.videoapi21.Response.JwtResponse;
import org.example.videoapi21.Response.RegisterResponse;
import org.example.videoapi21.Security.JwtUtils;
import org.example.videoapi21.Security.PasswordEncoderConfig;
import org.example.videoapi21.Security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class AuthUserService {

    private final AppUserRepository userRepository;
    private final PasswordValidation passwordValidation;
    private final PasswordEncoderConfig passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;

    private final Logger logger = LoggerFactory.getLogger(AuthUserService.class);

    public AuthUserService(AppUserRepository userRepository, PasswordValidation passwordValidation, PasswordEncoderConfig passwordEncoder, AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils, RoleRepository roleRepository, UserComponent userComponent) {
        this.userRepository = userRepository;
        this.passwordValidation = passwordValidation;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.roleRepository = roleRepository;
    }

    /**
     * If validations are successful register user.
     * @param registerRequest RegisterRequest object
     * @return ResponseEntity
     */
    public ResponseEntity<RegisterResponse> registerUser(RegisterRequest registerRequest) throws RegisterValidationUnsuccessfulException {

        ResponseEntity<?> validationResponse = registerValidations(registerRequest);
        if(validationResponse.getStatusCode().is4xxClientError()){
            throw new RegisterValidationUnsuccessfulException(Objects.toString(validationResponse.getBody(), "Validation failed"));
        }


        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));

        AppUser user = new AppUser(registerRequest.username(), passwordEncoder.passwordEncoder().encode(registerRequest.password()), role);
        userRepository.save(user);

        return ResponseEntity.ok(
                new RegisterResponse(user.getUsername())
        );
    }

    /**
     * Logs in user, generates JWT token
     * @return ResponseEntity with JWT token
     */
    public ResponseEntity<JwtResponse> loginUser(LoginRequest loginRequest) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getRoles()));
    }






//    /**
//     *  Validates email, password, username and checks if username already exists
//     *  @return ResponseEntity with message
//     */
    private ResponseEntity<String> registerValidations(RegisterRequest registerRequest){

        if(!passwordValidation.isValidPassword(registerRequest.password(), registerRequest.username(), 3)){
            return ResponseEntity.badRequest().body("Password validation unsuccessful");
        }
        if(registerRequest.username().length() < 3){
            return ResponseEntity.badRequest().body("Username must be at least 3 characters long");
        }
        if(userRepository.existsByUsername(registerRequest.username())){
            return ResponseEntity.badRequest().body("Username already exists");
        }
        return ResponseEntity.ok("Validations successful");
    }
}
