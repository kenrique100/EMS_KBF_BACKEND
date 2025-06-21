package com.kbf.employee.service.impl;

import com.kbf.employee.exception.FileStorageException;
import com.kbf.employee.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
    private String uploadDir;

    @PostConstruct
    @Override
    public void init() {
        try {
            Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(root);
            log.info("Initialized file storage at: {}", root);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String filename = generateUniqueFilename(file);
            Path destination = Paths.get(uploadDir).resolve(filename).normalize();

            // Ensure the path stays within the upload directory
            if (!destination.startsWith(Paths.get(uploadDir).normalize())) {
                throw new FileStorageException("Invalid file path attempted");
            }

            Files.createDirectories(destination.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("Stored file: {}", destination);
            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        if (filename == null || filename.isBlank()) {
            return; // Skip if filename is empty
        }

        try {
            Path file = Paths.get(uploadDir).resolve(filename).normalize();

            // Additional safety check
            if (!file.startsWith(Paths.get(uploadDir).normalize())) {
                throw new FileStorageException("Invalid file path: " + filename);
            }

            if (Files.exists(file)) {
                Files.delete(file);
                log.debug("Deleted file: {}", file);
            } else {
                log.warn("File not found, skipping deletion: {}", filename);
            }
        } catch (IOException e) {
            log.error("Error deleting file {}: {}", filename, e.getMessage());
            throw new FileStorageException("Failed to delete file: " + filename, e);
        }
    }

    private String generateUniqueFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String timestamp = String.valueOf(System.currentTimeMillis());

        if (originalFilename == null || originalFilename.isBlank()) {
            return timestamp + "_file";
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        String baseName = originalFilename.replace("." + extension, "");

        // Clean filename
        baseName = baseName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        return String.format("%s_%s%s",
                timestamp,
                baseName,
                extension != null ? "." + extension : "");
    }
}