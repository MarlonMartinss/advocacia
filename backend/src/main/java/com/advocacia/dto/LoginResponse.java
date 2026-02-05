package com.advocacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String type;
    private Long id;
    private String username;
    private String name;
    private String role;
    private List<String> allowedScreens;

    public static LoginResponse of(String token, Long id, String username, String name, String role, List<String> allowedScreens) {
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .id(id)
                .username(username)
                .name(name)
                .role(role)
                .allowedScreens(allowedScreens != null ? allowedScreens : List.of())
                .build();
    }
}
