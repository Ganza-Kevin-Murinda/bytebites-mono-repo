package com.auth_service.mapper;

import com.auth_service.dto.response.UserResponseDTO;
import com.auth_service.dto.response.UserSummaryDTO;
import com.auth_service.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponseDTO(User user);
    UserSummaryDTO toSummaryDTO(User user);
}
