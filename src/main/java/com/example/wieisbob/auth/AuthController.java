package com.example.wieisbob.auth;

import com.example.wieisbob.auth.dto.AuthResponse;
import com.example.wieisbob.auth.dto.LoginRequest;
import com.example.wieisbob.auth.dto.RegisterRequest;
import com.example.wieisbob.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authservice;

    @PostMapping("/register")
    public AuthResponse create(@RequestBody RegisterRequest request) {
        User user = authservice.register(request);
        Token token = authservice.createToken(user);

        return new AuthResponse(token.getToken(), token.getExpiresAt());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = authservice.login(request);
        Token token = authservice.createToken(user);

        return new AuthResponse(token.getToken(), token.getExpiresAt());
    }
}
