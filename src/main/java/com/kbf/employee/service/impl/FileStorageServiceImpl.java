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
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

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
            Files.createDirectories(root.resolve("profiles"));
            Files.createDirectories(root.resolve("documents"));
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        try {
            String filename = generateUniqueFilename(file.getOriginalFilename());
            Path destination = Paths.get(uploadDir)
                    .resolve(subDirectory)
                    .resolve(filename)
                    .normalize();

            Files.createDirectories(destination.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            Stream<Path> walk = Files.walk(Paths.get(uploadDir), 1);
            return walk
                    .onClose(walk::close)
                    .filter(path -> !path.equals(Paths.get(uploadDir)))
                    .map(Paths.get(uploadDir)::relativize);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename, String subDirectory) {
        return Paths.get(uploadDir)
                .resolve(subDirectory)
                .resolve(filename)
                .normalize();
    }

    @Override
    public Resource loadAsResource(String filename, String subDirectory) {
        try {
            Path file = load(filename, subDirectory);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
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
        try {
            FileSystemUtils.deleteRecursively(Paths.get(uploadDir));
            init();
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete all files", e);
        }
    }

    @Override
    public void delete(String filename, String subDirectory) {
        try {
            Path file = Paths.get(uploadDir)
                    .resolve(subDirectory)
                    .resolve(filename)
                    .normalize();

            if (!Files.deleteIfExists(file)) {
                throw new FileStorageException("File not found: " + filename);
            }
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public boolean fileExists(String filename, String subDirectory) {
        try {
            Path filePath = load(filename, subDirectory);
            return Files.exists(filePath) && Files.isRegularFile(filePath);
        } catch (FileStorageException e) {
            return false;
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String baseName = originalFilename.replace("." + extension, "");

        return String.format("%s_%s.%s", timestamp, baseName, extension);
    }
}