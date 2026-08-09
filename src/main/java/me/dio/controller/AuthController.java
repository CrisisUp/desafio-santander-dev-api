package me.dio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.dio.controller.dto.AuthDtos.AuthResponse;
import me.dio.controller.dto.AuthDtos.LoginRequest;
import me.dio.controller.dto.AuthDtos.LogoutRequest;
import me.dio.controller.dto.AuthDtos.RefreshRequest;
import me.dio.controller.dto.AuthDtos.RegisterRequest;
import me.dio.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "Login, registration, token refresh and logout (JWT).")
public record AuthController(AuthService authService) {

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive an access JWT + refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token pair issued"),
            @ApiResponse(responseCode = "422", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Create a new auth user (role USER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Auth user created with token pair"),
            @ApiResponse(responseCode = "422", description = "Username taken or invalid payload")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate a refresh token and receive a new token pair")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New token pair issued"),
            @ApiResponse(responseCode = "422", description = "Invalid, expired or revoked refresh token")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token (ends the session)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session revoked")
    })
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
