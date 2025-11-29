package org.example.videoapi21.Service;

import org.example.videoapi21.Component.UserComponent;
import org.example.videoapi21.DTO.RegisterValidationDTO;
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
import org.example.videoapi21.Security.Password.PasswordEncoderConfig;
import org.example.videoapi21.Security.Password.PasswordValidator;
import org.example.videoapi21.Security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class AuthUserService {

    private final AppUserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoderConfig passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;


    public AuthUserService(AppUserRepository userRepository, PasswordValidator passwordValidator, PasswordEncoderConfig passwordEncoder, AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils, RoleRepository roleRepository, UserComponent userComponent) {
        this.userRepository = userRepository;
        this.passwordValidator = passwordValidator;
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

        RegisterValidationDTO registerValidationDTO = registerValidations(registerRequest);
        if(!registerValidationDTO.isValid()){
            throw new RegisterValidationUnsuccessfulException(registerValidationDTO.message(), "Validation failed");
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
    private RegisterValidationDTO registerValidations(RegisterRequest registerRequest){
        String error = validationString(registerRequest);

        return (error == null)
                ? new RegisterValidationDTO(true)
                : new RegisterValidationDTO(false, error);
    }

    private String validationString(RegisterRequest r){
        if (!passwordValidator.isValidPassword(r.password(), r.username(), 3))
            return "Password validation unsuccessful";

        if (r.username().length() < 3)
            return "Username must be at least 3 characters long";

        if (userRepository.existsByUsername(r.username()))
            return "Username already exists";

        return null;
    }
}
