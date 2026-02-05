package com.advocacia.service;

import com.advocacia.dto.ScreenResponse;
import com.advocacia.entity.Screen;
import com.advocacia.entity.User;
import com.advocacia.entity.UserScreen;
import com.advocacia.repository.ScreenRepository;
import com.advocacia.repository.UserRepository;
import com.advocacia.repository.UserScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final ScreenRepository screenRepository;
    private final UserScreenRepository userScreenRepository;
    private final UserRepository userRepository;

    public List<ScreenResponse> findAllScreens() {
        return screenRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toScreenResponse)
                .collect(Collectors.toList());
    }

    public List<String> findScreenCodesByUserId(Long userId) {
        return userScreenRepository.findScreenCodesByUserId(userId);
    }

    @Transactional
    public void updateUserScreens(Long userId, List<String> screenCodes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        userScreenRepository.deleteByUserId(userId);

        if (screenCodes != null && !screenCodes.isEmpty()) {
            for (String code : screenCodes) {
                Screen screen = screenRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Tela não encontrada: " + code));
                UserScreen userScreen = UserScreen.builder()
                        .id(new UserScreen.UserScreenId(userId, screen.getId()))
                        .user(user)
                        .screen(screen)
                        .build();
                userScreenRepository.save(userScreen);
            }
        }
    }

    private ScreenResponse toScreenResponse(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .code(screen.getCode())
                .label(screen.getLabel())
                .route(screen.getRoute())
                .build();
    }
}
