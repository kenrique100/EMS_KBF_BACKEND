package com.kbf.employee.service.impl;

import com.kbf.employee.exception.FileStorageException;
import com.kbf.employee.exception.InvalidFileException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    // Allowed image file types
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/x-png",
            "image/pjpeg"
    );

    // Allowed document file types
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    // Maximum file size (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

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
            throw new FileStorageException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        validateFile(file, subDirectory);

        try {
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String filename = generateUniqueFilename(originalFilename);
            Path destinationFile = getDestinationPath(subDirectory, filename);

            Files.createDirectories(destinationFile.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
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
        Path filePath = Paths.get(uploadDir).resolve(subDirectory).resolve(filename).normalize();

        // Security check to prevent directory traversal
        Path rootPath = Paths.get(uploadDir).resolve(subDirectory).normalize();
        if (!filePath.getParent().equals(rootPath)) {
            throw new FileStorageException("Cannot access file outside of designated directory");
        }

        return filePath;
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
            init(); // Reinitialize the directories
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete all files", e);
        }
    }

    @Override
    public void delete(String filename, String subDirectory) {
        try {
            Path file = load(filename, subDirectory);
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

    private void validateFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        if (contentType == null || contentType.equals("multipart/form-data") || contentType.equals("application/octet-stream")) {
            // Fallback to determine content type from file extension
            contentType = determineContentTypeFromExtension(originalFilename);
            if (contentType == null) {
                throw new InvalidFileException("Could not determine file content type");
            }
        }

        contentType = contentType.toLowerCase();

        if (subDirectory.equals("profiles")) {
            boolean isValidImage = ALLOWED_IMAGE_TYPES.stream()
                    .map(String::toLowerCase)
                    .anyMatch(contentType::contains);

            if (!isValidImage) {
                throw new InvalidFileException(String.format(
                        "Only image files (%s) are allowed for profile pictures. Received: %s",
                        String.join(", ", ALLOWED_IMAGE_TYPES),
                        contentType
                ));
            }
        } else if (subDirectory.equals("documents")) {
            boolean isValidDocument = ALLOWED_DOCUMENT_TYPES.stream()
                    .map(String::toLowerCase)
                    .anyMatch(contentType::contains);

            if (!isValidDocument) {
                throw new InvalidFileException(String.format(
                        "Only document files (%s) are allowed. Received: %s",
                        String.join(", ", ALLOWED_DOCUMENT_TYPES),
                        contentType
                ));
            }
        }

        if (originalFilename.contains("..")) {
            throw new InvalidFileException("Filename contains invalid path sequence");
        }

        if (originalFilename.length() > 100) {
            throw new InvalidFileException("Filename is too long (max 100 characters)");
        }
    }

    private String determineContentTypeFromExtension(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        if (extension == null) {
            return null;
        }

        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> null;
        };
    }

    private Path getDestinationPath(String subDirectory, String filename) {
        Path destinationFile = Paths.get(uploadDir)
                .resolve(subDirectory)
                .resolve(filename)
                .normalize()
                .toAbsolutePath();

        // Security check to prevent directory traversal
        Path rootPath = Paths.get(uploadDir).resolve(subDirectory).normalize().toAbsolutePath();
        if (!destinationFile.getParent().equals(rootPath)) {
            throw new FileStorageException("Cannot store file outside of designated directory");
        }

        return destinationFile;
    }

    private String generateUniqueFilename(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileExtension = "";

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        return timestamp + "_" + originalFilename.replace(fileExtension, "") + fileExtension;
    }
}