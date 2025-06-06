package com.kbf.employee.service.impl;

import com.kbf.employee.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Files.createDirectories(uploadPath.resolve("profiles"));
            Files.createDirectories(uploadPath.resolve("documents"));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        try {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path destinationFile = Paths.get(uploadDir)
                    .resolve(subDirectory)
                    .resolve(filename)
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(Paths.get(uploadDir).resolve(subDirectory).toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try (Stream<Path> walk = Files.walk(Paths.get(uploadDir), 1)) {
            return walk
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
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(Paths.get(uploadDir).toFile());
    }

    @Override
    public void delete(String filename, String subDirectory) {
        try {
            Files.deleteIfExists(Paths.get(uploadDir).resolve(subDirectory).resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}