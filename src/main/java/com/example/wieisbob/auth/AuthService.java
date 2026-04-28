package com.example.wieisbob.auth;

import com.example.wieisbob.auth.dto.LoginRequest;
import com.example.wieisbob.auth.dto.RegisterRequest;
import com.example.wieisbob.user.User;
import com.example.wieisbob.user.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(BCrypt.hashpw(request.password(), BCrypt.gensalt(12)));

        return userRepository.save(user);
    }

    public Token createToken(User user) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expiration);
        LocalDateTime expiresAtLocal = expiresAt.toInstant()
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();

        String jwt = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(getSigningKey())
                .compact();

        Token token = new Token();
        token.setToken(jwt);
        token.setUser(user);
        token.setExpiresAt(expiresAtLocal);

        tokenRepository.save(token);

        return token;
    }


    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!BCrypt.checkpw(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        return user;
    }
}
