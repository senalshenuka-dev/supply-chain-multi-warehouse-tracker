package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.domain.model.AuditLog;
import com.supplychainmultiwarehousetracker.domain.model.Role;
import com.supplychainmultiwarehousetracker.domain.model.User;
import com.supplychainmultiwarehousetracker.domain.repository.AuditLogRepository;
import com.supplychainmultiwarehousetracker.domain.repository.UserRepository;
import com.supplychainmultiwarehousetracker.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        Page<AuditLog> auditLogsPage = auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
        return ResponseEntity.ok(auditLogsPage.getContent());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody com.supplychainmultiwarehousetracker.dto.RegisterRequest request, Principal principal) {
        String adminUsername = principal != null ? principal.getName() : "admin";
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.ROLE_MANAGER)
                .build();

        User savedUser = userRepository.save(user);
        auditLogService.log("User", savedUser.getId().toString(), "ADMIN_CREATE_USER", adminUsername,
                "Admin created user account " + savedUser.getUsername() + " with role " + savedUser.getRole());

        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody com.supplychainmultiwarehousetracker.dto.RegisterRequest request,
            Principal principal
    ) {
        String adminUsername = principal != null ? principal.getName() : "admin";
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email is already registered");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User updated = userRepository.save(user);
        auditLogService.log("User", updated.getId().toString(), "ADMIN_UPDATE_USER", adminUsername,
                "Admin updated account details for " + updated.getUsername());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, Principal principal) {
        String adminUsername = principal != null ? principal.getName() : "admin";
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (user.getUsername().equalsIgnoreCase(adminUsername)) {
            throw new IllegalArgumentException("Cannot delete currently authenticated admin account");
        }

        userRepository.delete(user);
        auditLogService.log("User", id.toString(), "ADMIN_DELETE_USER", adminUsername,
                "Admin deleted user account " + user.getUsername());

        return ResponseEntity.ok(Map.of("message", "User account " + user.getUsername() + " deleted successfully"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "admin";
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        String newRoleStr = payload.get("role");
        Role newRole = Role.valueOf(newRoleStr);
        String oldRole = user.getRole().name();
        user.setRole(newRole);

        User updated = userRepository.save(user);
        auditLogService.log("User", user.getId().toString(), "USER_ROLE_CHANGE", username,
                "Updated role for " + user.getUsername() + " from " + oldRole + " to " + newRoleStr);

        return ResponseEntity.ok(updated);
    }
}
