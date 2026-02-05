package com.advocacia.controller;

import com.advocacia.dto.ScreenResponse;
import com.advocacia.service.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    @GetMapping("/screens")
    public ResponseEntity<List<ScreenResponse>> findAllScreens() {
        return ResponseEntity.ok(userPermissionService.findAllScreens());
    }

    @GetMapping("/users/{id}/screens")
    public ResponseEntity<List<String>> getUserScreens(@PathVariable Long id) {
        return ResponseEntity.ok(userPermissionService.findScreenCodesByUserId(id));
    }

    @PutMapping("/users/{id}/screens")
    public ResponseEntity<Void> updateUserScreens(@PathVariable Long id, @RequestBody List<String> screenCodes) {
        userPermissionService.updateUserScreens(id, screenCodes);
        return ResponseEntity.noContent().build();
    }
}
