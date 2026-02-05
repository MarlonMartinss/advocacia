package com.advocacia.config;

import com.advocacia.entity.Screen;
import com.advocacia.entity.User;
import com.advocacia.entity.UserScreen;
import com.advocacia.repository.ScreenRepository;
import com.advocacia.repository.UserRepository;
import com.advocacia.repository.UserScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "1234";

    private final UserRepository userRepository;
    private final ScreenRepository screenRepository;
    private final UserScreenRepository userScreenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeScreens();
        initializeAdminUser();
    }

    private void initializeScreens() {
        if (screenRepository.count() > 0) return;

        List<Screen> screens = List.of(
                Screen.builder().code("dashboard").label("Dashboard").route("/dashboard").displayOrder(0).build(),
                Screen.builder().code("contratos").label("Contratos").route("/contratos").displayOrder(1).build(),
                Screen.builder().code("permissoes").label("Permissões usuários").route("/permissoes-usuarios").displayOrder(2).build(),
                Screen.builder().code("usuarios").label("Usuários").route("/usuarios").displayOrder(3).build()
        );
        screenRepository.saveAll(screens);
        log.info("Telas iniciais criadas");
    }

    private void initializeAdminUser() {
        userRepository.findByUsername(ADMIN_USERNAME).ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                    userRepository.save(user);
                    assignAllScreensToUser(user);
                    log.info("Senha do usuário admin atualizada com sucesso");
                },
                () -> {
                    User admin = User.builder()
                            .username(ADMIN_USERNAME)
                            .password(passwordEncoder.encode(ADMIN_PASSWORD))
                            .name("Administrador")
                            .email("admin@advocacia.com")
                            .role("ADMIN")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    admin = userRepository.save(admin);
                    assignAllScreensToUser(admin);
                    log.info("Usuário admin criado com sucesso (login: admin, senha: 1234)");
                }
        );
    }

    private void assignAllScreensToUser(User user) {
        if (userScreenRepository.findScreenCodesByUserId(user.getId()).size() > 0) return;
        List<Screen> screens = screenRepository.findAllByOrderByDisplayOrderAsc();
        for (Screen screen : screens) {
            UserScreen us = UserScreen.builder()
                    .id(new UserScreen.UserScreenId(user.getId(), screen.getId()))
                    .user(user)
                    .screen(screen)
                    .build();
            userScreenRepository.save(us);
        }
    }
}
