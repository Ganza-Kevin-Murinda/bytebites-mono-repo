package com.auth_service.controller;

import com.auth_service.dto.response.ApiResponseDTO;
import com.auth_service.dto.response.UserResponseDTO;
import com.auth_service.dto.response.UserSummaryDTO;
import com.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return users != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Fetched all users", users))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Developer not found", 404));
    }

}