package me.dio.service;

import me.dio.config.JwtService;
import me.dio.controller.dto.AuthDtos.AuthResponse;
import me.dio.controller.dto.AuthDtos.LoginRequest;
import me.dio.controller.dto.AuthDtos.RegisterRequest;
import me.dio.domain.model.AuthUser;
import me.dio.domain.repository.AuthUserRepository;
import me.dio.service.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthUserRepository authUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Invalid username or password."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Invalid username or password.");
        }
        return toResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already taken.");
        }
        AuthUser user = new AuthUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user = authUserRepository.save(user);
        return toResponse(user);
    }

    private AuthResponse toResponse(AuthUser user) {
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getUserId());
        return new AuthResponse(token, user.getUsername(), user.getRole(), user.getUserId());
    }
}
