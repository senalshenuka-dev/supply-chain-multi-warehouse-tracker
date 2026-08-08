package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.dto.AuthResponse;
import com.supplychainmultiwarehousetracker.dto.LoginRequest;
import com.supplychainmultiwarehousetracker.dto.RegisterRequest;
import com.supplychainmultiwarehousetracker.dto.UserDto;
import com.supplychainmultiwarehousetracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto currentUser = authService.getCurrentUserDto(authentication.getName());
        return ResponseEntity.ok(currentUser);
    }
}
