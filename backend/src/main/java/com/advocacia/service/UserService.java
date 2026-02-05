package com.advocacia.service;

import com.advocacia.dto.UserRequest;
import com.advocacia.dto.UserResponse;
import com.advocacia.entity.User;
import com.advocacia.exception.DuplicateUsernameException;
import com.advocacia.exception.UserNotFoundException;
import com.advocacia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        return toResponse(user);
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("Usuário já existe com este login");
        }
        String name = (request.getName() != null && !request.getName().isBlank()) ? request.getName() : request.getUsername();
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "1234"))
                .name(name)
                .email(request.getEmail())
                .role(request.getRole() != null ? request.getRole() : "USER")
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("Usuário já existe com este login");
        }

        user.setUsername(request.getUsername());
        user.setName((request.getName() != null && !request.getName().isBlank()) ? request.getName() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : user.getRole());
        user.setActive(request.getActive() != null ? request.getActive() : user.getActive());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        // Obter username do usuário logado de forma segura
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername;
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else {
            currentUsername = principal.toString();
        }

        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Não é possível excluir seu próprio usuário");
        }

        userRepository.delete(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
