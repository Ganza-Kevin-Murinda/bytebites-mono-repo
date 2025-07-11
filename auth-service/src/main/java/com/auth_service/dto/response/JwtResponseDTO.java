package com.auth_service.dto.response;

import com.auth_service.model.ERole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO containing the JWT and user info")
public class JwtResponseDTO {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String token;

    @Schema(description = "Token type (usually 'Bearer')", example = "Bearer")
    private String tokenType;

    @Schema(description = "Logged-in user's username/email", example = "john.doe@example.com")
    private String username;

    @Schema(description = "Logged-in user's Id", example = "a81bc81b-dead-4e5d-abff-90865d1e13b1")
    private String id;

    @Schema(description = "User's role", example = "ROLE_DEVELOPER")
    private ERole role;
}
