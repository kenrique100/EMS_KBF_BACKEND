package com.kbf.employee.service.impl;

import com.kbf.employee.exception.FileStorageException;
import com.kbf.employee.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${file.upload-dir:uploads}")
    private String baseUploadDir;

    @PostConstruct
    @Override
    public void init() {
        try {
            Path root = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(root);
            log.info("Initialized file storage at: {}", root);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory, String filename) {
        try {
            Path destination = buildDestinationPath(subDirectory, filename);
            Files.createDirectories(destination.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return destination.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + filename, e);
        }
    }

    @Override
    public String store(InputStream inputStream, long size, String subDirectory, String filename, String contentType) {
        try {
            Path destination = buildDestinationPath(subDirectory, filename);
            Files.createDirectories(destination.getParent());
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + filename, e);
        }
    }

    @Override
    public Resource loadAsResource(String filePath) {
        try {
            Path file = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Could not read file: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read file: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            Path file = Paths.get(filePath).normalize();
            if (Files.exists(file)) {
                Files.delete(file);
                log.debug("Deleted file: {}", file);
            }
        } catch (IOException e) {
            log.error("Error deleting file {}: {}", filePath, e.getMessage());
            throw new FileStorageException("Failed to delete file: " + filePath, e);
        }
    }

    private Path buildDestinationPath(String subDirectory, String filename) {
        Path destination = Paths.get(baseUploadDir, subDirectory, filename).toAbsolutePath().normalize();

        // Security check to prevent directory traversal
        Path basePath = Paths.get(baseUploadDir).toAbsolutePath().normalize();
        if (!destination.startsWith(basePath)) {
            throw new FileStorageException("Cannot store file outside upload directory");
        }

        return destination;
    }
}