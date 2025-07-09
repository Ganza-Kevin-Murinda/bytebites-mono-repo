package com.auth_service.service;

import com.auth_service.auth.oauth2.OAuth2UserInfo;
import com.auth_service.dto.response.UserResponseDTO;
import com.auth_service.dto.response.UserSummaryDTO;
import com.auth_service.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    List<UserResponseDTO> getAllUsers();

    User createUser(User user); // for local registration

    User registerOAuth2User(OAuth2UserInfo info); // for OAuth2 callback

    UserSummaryDTO getCurrentUserDetails(); // from SecurityContext

}
