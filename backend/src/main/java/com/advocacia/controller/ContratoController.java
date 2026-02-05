package com.advocacia.controller;

import com.advocacia.dto.ContratoRequest;
import com.advocacia.dto.ContratoResponse;
import com.advocacia.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<ContratoResponse>> findAll() {
        return ResponseEntity.ok(contratoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ContratoResponse> create(@RequestBody(required = false) ContratoRequest request) {
        ContratoResponse response = contratoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContratoResponse> update(@PathVariable Long id, @RequestBody ContratoRequest request) {
        return ResponseEntity.ok(contratoService.update(id, request));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ContratoResponse> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.finalizar(id));
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll() {
        contratoService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contratoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
