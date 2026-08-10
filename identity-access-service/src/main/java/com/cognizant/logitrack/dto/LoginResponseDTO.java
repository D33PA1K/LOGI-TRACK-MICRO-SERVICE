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

    /**
     * Short-lived access JWT. Still named "token" so existing clients keep
     * working; {@link #expiresInSeconds} tells the client when to refresh it.
     */
    private String token;

    /** Long-lived, revocable, opaque refresh token. Absent on a refresh that did not rotate. */
    private String refreshToken;

    /** Access-token lifetime in seconds, so the client never has to decode the JWT to know. */
    private Long expiresInSeconds;

    private String role;
    private Integer userId;
    private String name;
}
