package com.advocacia.service;

import com.advocacia.dto.LoginRequest;
import com.advocacia.dto.LoginResponse;
import com.advocacia.entity.User;
import com.advocacia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserPermissionService userPermissionService;

    public LoginResponse authenticate(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));

        String token = jwtService.generateToken(user);

        List<String> allowedScreens = userPermissionService.findScreenCodesByUserId(user.getId());

        return LoginResponse.of(
                token,
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole(),
                allowedScreens
        );
    }
}
