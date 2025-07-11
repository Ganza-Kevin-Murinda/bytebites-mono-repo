package com.auth_service.controller;

import com.auth_service.dto.request.CreateUserRequestDTO;
import com.auth_service.dto.response.ApiResponseDTO;
import com.auth_service.dto.response.JwtResponseDTO;
import com.auth_service.dto.response.UserSummaryDTO;
import com.auth_service.model.User;
import com.auth_service.service.UserService;
import com.auth_service.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<String>> registerUser(@Valid @RequestBody CreateUserRequestDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());

        User createdUser = userService.createUser(user);

        return createdUser != null
                ? ResponseEntity.ok(ApiResponseDTO.created("User registered successfully"))
                : ResponseEntity.badRequest().body(ApiResponseDTO.created("User not found"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<JwtResponseDTO>> loginUser(@Valid @RequestBody CreateUserRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user.getId().toString(),user.getUsername(),user.getRole().name());

        JwtResponseDTO response = JwtResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .id(user.getId().toString())
                .role(user.getRole())
                .build();

        return token != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Login successful", response))
                : ResponseEntity.status(401).body(ApiResponseDTO.error("Invalid credentials", 401));

    }
    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<UserSummaryDTO>> getCurrentUser() {
        UserSummaryDTO currentUser = userService.getCurrentUserDetails();
        return currentUser != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Current user retrieved", currentUser))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("User not found", 404));
    }
}