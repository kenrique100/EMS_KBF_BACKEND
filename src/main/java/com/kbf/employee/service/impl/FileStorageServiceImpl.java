package com.kbf.employee.service.impl;

import com.kbf.employee.exception.FileStorageException;
import com.kbf.employee.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostConstruct
    @Override
    public void init() {
        try {
            Path rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("profiles"));
            Files.createDirectories(rootLocation.resolve("documents"));
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file");
            }

            // Validate file name
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            if (originalFilename.contains("..")) {
                throw new FileStorageException("Cannot store file with relative path outside current directory");
            }

            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new FileStorageException("File size exceeds 5MB limit");
            }

            // Validate file extension
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (subDirectory.equals("profiles") && !List.of("jpg", "jpeg", "png", "gif").contains(fileExtension)) {
                throw new FileStorageException("Only image files (JPG, JPEG, PNG, GIF) are allowed for profile pictures");
            }

            if (subDirectory.equals("documents") && !List.of("pdf", "doc", "docx").contains(fileExtension)) {
                throw new FileStorageException("Only PDF and Word documents are allowed");
            }

            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Path destinationFile = Paths.get(uploadDir)
                    .resolve(subDirectory)
                    .resolve(filename)
                    .normalize()
                    .toAbsolutePath();

            // Security check
            if (!destinationFile.getParent().equals(Paths.get(uploadDir).resolve(subDirectory).toAbsolutePath())) {
                throw new FileStorageException("Cannot store file outside current directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(Paths.get(uploadDir), 1)
                    .filter(path -> !path.equals(Paths.get(uploadDir)))
                    .map(Paths.get(uploadDir)::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename, String subDirectory) {
        return Paths.get(uploadDir).resolve(subDirectory).resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename, String subDirectory) {
        try {
            Path file = load(filename, subDirectory);
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
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(Paths.get(uploadDir).toFile());
    }

    @Override
    public void delete(String filename, String subDirectory) {
        try {
            Path file = load(filename, subDirectory);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}