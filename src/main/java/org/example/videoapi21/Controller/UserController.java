package org.example.videoapi21.Controller;


import org.example.videoapi21.Request.LoginRequest;
import org.example.videoapi21.Request.RegisterRequest;
import org.example.videoapi21.Response.JwtResponse;
import org.example.videoapi21.Response.RegisterResponse;
import org.example.videoapi21.Service.AuthUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
public class UserController {
    private final AuthUserService authUserService;

    public UserController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }



    @PostMapping("/auth/register")
    public ResponseEntity<RegisterResponse> userRegister(@RequestBody RegisterRequest registerRequest){
         return authUserService.registerUser(registerRequest);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<JwtResponse> userLogin(@RequestBody LoginRequest loginRequest){
        return  authUserService.loginUser(loginRequest);
    }

}
