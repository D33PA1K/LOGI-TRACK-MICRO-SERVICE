package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.Role;
import com.cognizant.logitrack.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userId;
    private String name;
    private Role role;
    private String email;
    private String phone;
    private Integer hubId;
    private UserStatus status;
}
