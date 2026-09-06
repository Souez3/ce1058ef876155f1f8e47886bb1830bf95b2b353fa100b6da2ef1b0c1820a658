package com.example.darkstore.presentation.dto.mappers;

import com.example.darkstore.dataAccess.models.User;
import com.example.darkstore.presentation.dto.objects.user.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
