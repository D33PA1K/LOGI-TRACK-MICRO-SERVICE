package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.RegisterRequestDTO;
import com.cognizant.logitrack.dto.UserDTO;
import com.cognizant.logitrack.enums.UserStatus;
import java.util.List;

public interface UserService {
    UserDTO createUser(RegisterRequestDTO dto);
    UserDTO getUserById(Integer id);
    List<UserDTO> getAllUsers();
    UserDTO updateUserStatus(Integer id, UserStatus status);
    void deleteUser(Integer id);
}
