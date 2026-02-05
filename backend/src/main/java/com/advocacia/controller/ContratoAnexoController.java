package com.advocacia.controller;

import com.advocacia.dto.ContratoAnexoResponse;
import com.advocacia.entity.Contrato;
import com.advocacia.entity.ContratoAnexo;
import com.advocacia.repository.ContratoAnexoRepository;
import com.advocacia.repository.ContratoRepository;
import com.advocacia.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contratos/{contratoId}/anexos")
@RequiredArgsConstructor
public class ContratoAnexoController {

    private final ContratoRepository contratoRepository;
    private final ContratoAnexoRepository anexoRepository;
    private final FileStorageService fileStorageService;

    /**
     * Lista todos os anexos de um contrato.
     */
    @GetMapping
    public ResponseEntity<List<ContratoAnexoResponse>> listarAnexos(@PathVariable Long contratoId) {
        // Verificar se contrato existe
        if (!contratoRepository.existsById(contratoId)) {
            return ResponseEntity.notFound().build();
        }

        List<ContratoAnexoResponse> anexos = anexoRepository.findByContratoIdOrderByCreatedAtDesc(contratoId)
                .stream()
                .map(ContratoAnexoResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(anexos);
    }

    /**
     * Faz upload de um arquivo para o contrato.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<ContratoAnexoResponse> uploadAnexo(
            @PathVariable Long contratoId,
            @RequestParam("file") MultipartFile file) {

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        // Armazenar arquivo
        String nomeArquivo = fileStorageService.store(file);

        // Criar registro no banco
        ContratoAnexo anexo = ContratoAnexo.builder()
                .contrato(contrato)
                .nomeOriginal(file.getOriginalFilename())
                .nomeArquivo(nomeArquivo)
                .tipoMime(file.getContentType())
                .tamanho(file.getSize())
                .build();

        anexo = anexoRepository.save(anexo);

        return ResponseEntity.status(HttpStatus.CREATED).body(ContratoAnexoResponse.fromEntity(anexo));
    }

    /**
     * Baixa um anexo específico.
     */
    @GetMapping("/{anexoId}/download")
    public ResponseEntity<Resource> downloadAnexo(
            @PathVariable Long contratoId,
            @PathVariable Long anexoId) {

        ContratoAnexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado"));

        // Verificar se o anexo pertence ao contrato
        if (!anexo.getContrato().getId().equals(contratoId)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = fileStorageService.load(anexo.getNomeArquivo());

        String contentType = anexo.getTipoMime();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + anexo.getNomeOriginal() + "\"")
                .body(resource);
    }

    /**
     * Exclui um anexo específico.
     */
    @DeleteMapping("/{anexoId}")
    @Transactional
    public ResponseEntity<Void> excluirAnexo(
            @PathVariable Long contratoId,
            @PathVariable Long anexoId) {

        ContratoAnexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado"));

        // Verificar se o anexo pertence ao contrato
        if (!anexo.getContrato().getId().equals(contratoId)) {
            return ResponseEntity.notFound().build();
        }

        // Excluir arquivo do disco
        fileStorageService.delete(anexo.getNomeArquivo());

        // Excluir registro do banco
        anexoRepository.delete(anexo);

        return ResponseEntity.noContent().build();
    }
}
