package com.advocacia.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Nome de usuário é obrigatório")
    @Size(min = 1, max = 100)
    private String username;

    @Size(min = 4, max = 100)
    private String password; // obrigatório no create; opcional no update

    @Size(max = 150)
    private String name; // opcional; se null/blank o backend usa username

    @Size(max = 150)
    private String email; // opcional

    @NotBlank(message = "Perfil é obrigatório")
    @Size(max = 50)
    @Builder.Default
    private String role = "USER";

    @Builder.Default
    private Boolean active = true;

    public void setPassword(String password) {
        this.password = (password != null && !password.isBlank()) ? password : null;
    }
}
