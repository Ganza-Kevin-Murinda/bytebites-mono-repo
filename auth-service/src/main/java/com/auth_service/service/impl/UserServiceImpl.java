package com.auth_service.service.impl;

import com.auth_service.auth.oauth2.OAuth2UserInfo;
import com.auth_service.dto.response.UserResponseDTO;
import com.auth_service.dto.response.UserSummaryDTO;
import com.auth_service.exception.DuplicateResourceException;
import com.auth_service.exception.UserNotFoundException;
import com.auth_service.mapper.UserMapper;
import com.auth_service.model.EAuthProvider;
import com.auth_service.model.ERole;
import com.auth_service.model.User;
import com.auth_service.repository.UserRepository;
import com.auth_service.service.UserService;
import com.auth_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;


    @Override
    public User createUser(User user) {
        log.info("Registering new local user: {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("User with email already exists: " + user.getUsername());
        }

        // Set defaults
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvider(EAuthProvider.LOCAL);
        user.setRole(ERole.ROLE_RESTAURANT_OWNER); // assign developer role by default

        User savedUser = userRepository.save(user);
        log.info("Successfully registered local user with ID: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAllUserDetails()
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Used during OAuth2 callback to register or return existing user.
     */
    @Override
    public User registerOAuth2User(OAuth2UserInfo info) {
        log.info("Google OAuth2 login attempt for email: {}", info.getEmail());

        return userRepository.findByUsername(info.getEmail())
                .orElseGet(() -> {
                    log.info(">>> User not found, creating new one...");
                    User newUser = new User();
                    newUser.setUsername(info.getEmail());
                    newUser.setPassword("");
                    newUser.setAuthProvider(EAuthProvider.GOOGLE);
                    newUser.setRole(ERole.ROLE_CUSTOMER); // Default role for OAuth2 users

                    User savedUser = userRepository.save(newUser);
                    log.info("New Google OAuth2 user created: {}", savedUser.getUsername());

                    return savedUser;
                });
    }

    /**
     * Retrieve current authenticated user.
     */
    @Override
    public UserSummaryDTO getCurrentUserDetails() {
        log.info("Getting user's details");
        return userMapper.toSummaryDTO(getCurrentUser());
    }

    public User getCurrentUser() {
        String email = jwtUtil.getCurrentUserEmail()
                .orElseThrow(() -> new UserNotFoundException("No authenticated user found"));

        return userRepository.findByUsername(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(()->new UsernameNotFoundException("User with username " + username + " not found"));
    }
}

