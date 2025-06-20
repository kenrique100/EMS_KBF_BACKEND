package com.kbf.employee.util;

import com.kbf.employee.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidationUtil {
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public static void validateImageFile(MultipartFile file) throws InvalidFileException {
        if (file == null || file.isEmpty()) return;
        validateFile(file, "image");
    }

    public static void validateDocumentFile(MultipartFile file) throws InvalidFileException {
        if (file == null || file.isEmpty()) return;
        validateFile(file, "document");
    }

    private static void validateFile(MultipartFile file, String fileType) throws InvalidFileException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (contentType == null || contentType.equals("multipart/form-data")) {
            contentType = determineContentTypeFromFilename(filename);
        }

        if (contentType == null) {
            throw new InvalidFileException("Could not determine file type");
        }

        contentType = contentType.toLowerCase();

        if (fileType.equals("image") && !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidFileException(String.format(
                    "Only image files (%s) are allowed. Received: %s",
                    String.join(", ", ALLOWED_IMAGE_TYPES),
                    contentType
            ));
        }

        if (fileType.equals("document") && !ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new InvalidFileException(String.format(
                    "Only document files (%s) are allowed. Received: %s",
                    String.join(", ", ALLOWED_DOCUMENT_TYPES),
                    contentType
            ));
        }

        if (filename != null && filename.contains("..")) {
            throw new InvalidFileException("Filename contains invalid path sequence");
        }
    }

    private static String determineContentTypeFromFilename(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> null;
        };
    }
}