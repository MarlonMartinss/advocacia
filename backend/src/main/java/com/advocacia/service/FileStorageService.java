package com.advocacia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de upload: " + uploadDir, e);
        }
    }

    /**
     * Armazena o arquivo no disco e retorna o nome único gerado.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Arquivo vazio ou nulo");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Validar nome do arquivo
        if (originalFilename.contains("..")) {
            throw new RuntimeException("Nome de arquivo inválido: " + originalFilename);
        }

        // Gerar nome único
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao armazenar arquivo: " + originalFilename, e);
        }
    }

    /**
     * Carrega o arquivo como Resource para download.
     */
    public Resource load(String filename) {
        try {
            Path filePath = this.uploadPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Arquivo não encontrado: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Arquivo não encontrado: " + filename, e);
        }
    }

    /**
     * Remove o arquivo do disco.
     */
    public void delete(String filename) {
        try {
            Path filePath = this.uploadPath.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao excluir arquivo: " + filename, e);
        }
    }

    /**
     * Retorna o caminho completo do arquivo.
     */
    public Path getFilePath(String filename) {
        return this.uploadPath.resolve(filename).normalize();
    }
}
