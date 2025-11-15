package org.example.videoapi21.Service;

import org.example.videoapi21.Component.PasswordValidation;
import org.example.videoapi21.Component.UserComponent;
import org.example.videoapi21.Entity.AppUser;
import org.example.videoapi21.Entity.Role;
import org.example.videoapi21.Enum.ERole;
import org.example.videoapi21.Exception.RegisterValidationUnsuccessfulException;
import org.example.videoapi21.Exception.UserAlreadyExistsException;
import org.example.videoapi21.Repository.AppUserRepository;
import org.example.videoapi21.Repository.RoleRepository;
import org.example.videoapi21.Request.RegisterRequest;
import org.example.videoapi21.Response.RegisterResponse;
import org.example.videoapi21.Security.JwtUtils;
import org.example.videoapi21.Security.PasswordEncoderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

@Service
public class AuthUserService {

    private final AppUserRepository userRepository;
    private final PasswordValidation passwordValidation;
    private final PasswordEncoderConfig passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final UserComponent userComponent;

    private final Logger logger = LoggerFactory.getLogger(AuthUserService.class);

    public AuthUserService(AppUserRepository userRepository, PasswordValidation passwordValidation, PasswordEncoderConfig passwordEncoder, AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils, RoleRepository roleRepository, UserComponent userComponent) {
        this.userRepository = userRepository;
        this.passwordValidation = passwordValidation;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.roleRepository = roleRepository;
        this.userComponent = userComponent;
    }

    /**
     * If validations are successful, register user and generates confirmation code and sends email. Account is not active
     * until user confirms operation!
     * @param registerRequest RegisterRequest object
     * @return ResponseEntity with message
     */
    public ResponseEntity<RegisterResponse> registerUser(RegisterRequest registerRequest) throws UserAlreadyExistsException, RegisterValidationUnsuccessfulException {

        ResponseEntity<?> validationResponse = registerValidations(registerRequest);
        if(validationResponse.getStatusCode().is4xxClientError()){
            throw new RegisterValidationUnsuccessfulException(validationResponse.toString());
        }
        if(userRepository.existsByUsername(registerRequest.username())){
            throw new UserAlreadyExistsException("Username is already taken");
        }

        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));

        AppUser user = new AppUser(registerRequest.username(), passwordEncoder.passwordEncoder().encode(registerRequest.password()), role);
        userRepository.save(user);

        return ResponseEntity.ok(
                new RegisterResponse(user.getUsername())
        );
    }

//    /**
//     * Logs in user, generates JWT token
//     * @return ResponseEntity with JWT token
//     */
//    public ResponseEntity<?> loginUser(LoginRequest loginRequest){
//        try{
//            logger.info("Attempting login for user: {}", loginRequest.getUsername());
//            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            logger.info("User {} successfully authenticated", loginRequest.getUsername());
//            String jwt = jwtUtils.generateJwtToken(authentication);
//            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//            Set<Role> roles = userDetails.getAuthorities().stream()
//                    .map(authority -> {
//                        String roleName = authority.getAuthority();
//                        ERole role = ERole.valueOf(roleName);
//                        return new Role(role);
//                    }).collect(Collectors.toSet());
//
////            if(!userDetails.isActive()){
////                return ResponseEntity.ok("User not activated");
////            }
//            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
//        }catch (Exception e){
//            logger.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad credentials");
//        }
//    }


//    public ResponseEntity<?> forgotPassword(String email){
//        Optional<User> userOptional = userRepository.findByEmail(email);
//        if(userOptional.isEmpty()){
//            return ResponseEntity.badRequest().body("User not found");
//        }
//        User user = userOptional.get();
//        user.setConfirmationCode(generateConfirmationCode());
//        userRepository.save(user);
//        sendConfirmationCodeMail(user.getEmail(), user);
//        return ResponseEntity.ok("Confirmation code sent to email");
//    }

//    /**
//     * Changes password if user is found by email and confirmation code is correct. Password must be valid at password strength 3
//     * @param resetPasswordRequest ResetPasswordRequest object
//     * @return ResponseEntity with message
//     */
//    public ResponseEntity<?> changePassword(ResetPasswordRequest resetPasswordRequest){
//        Optional<User> userOptional = userRepository.findByEmail(resetPasswordRequest.getEmail());
//        if(userOptional.isEmpty()){
//            return ResponseEntity.badRequest().body("Invalid confirmation code");
//        }
//        User user = userOptional.get();
//
//        if(!passwordValidation.isValidPassword(resetPasswordRequest.getNewPassword(), user.getUsername(), 3)){
//            return ResponseEntity.badRequest().body("Invalid password");
//        }
//        else{
//            user.setPassword(passwordEncoder.passwordEncoder().encode(resetPasswordRequest.getNewPassword()));
//            user.setConfirmationCode(null);
//            userRepository.save(user);
//            return ResponseEntity.ok("Password changed successfully");
//        }
//
//    }



//    /**
//     *  Validates email, password, username and checks if username already exists
//     *  @return ResponseEntity with message
//     */
    private ResponseEntity<String> registerValidations(RegisterRequest registerRequest){

        if(!passwordValidation.isValidPassword(registerRequest.password(), registerRequest.username(), 3)){
            return ResponseEntity.badRequest().body("Password validation unsuccessful");
        }
        if(userRepository.existsByUsername(registerRequest.username())){
            return ResponseEntity.badRequest().body("Username already exists");
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
