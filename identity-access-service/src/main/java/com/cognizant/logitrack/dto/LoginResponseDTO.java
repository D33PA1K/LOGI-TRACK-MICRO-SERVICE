package com.cognizant.logitrack.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {

    /** The stateless access JWT used for all authenticated calls. */
    private String token;

    /** Access-token lifetime in seconds, so the client never has to decode the JWT to know. */
    private Long expiresInSeconds;

    private String role;
    private Integer userId;
    private String name;
}
