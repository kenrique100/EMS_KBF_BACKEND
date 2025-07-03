package com.kbf.employee.service.impl;

import com.kbf.employee.dto.request.EmployeeDTO;
import com.kbf.employee.exception.*;
import com.kbf.employee.model.Employee;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.service.EmployeeProfilePictureService;
import com.kbf.employee.service.FileStorageService;
import com.kbf.employee.util.EmployeeConverter;
import com.kbf.employee.util.FileValidationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeProfilePictureServiceImpl implements EmployeeProfilePictureService {
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final EmployeeConverter employeeConverter;

    @Value("${profile.picture.upload-dir:profile-pictures}")
    private String uploadDir;

    @Value("${profile.picture.thumbnail.width:150}")
    private int thumbnailWidth;

    @Value("${profile.picture.thumbnail.height:150}")
    private int thumbnailHeight;

    @Transactional
    public String uploadProfilePicture(Long employeeId, MultipartFile file) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        FileValidationUtil.validateImageFile(file);

        try {
            // Delete existing images
            if (employee.getProfilePicturePath() != null) {
                fileStorageService.delete(employee.getProfilePicturePath());
            }
            if (employee.getProfilePictureThumbnailPath() != null) {
                fileStorageService.delete(employee.getProfilePictureThumbnailPath());
            }

            // Get file extension
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            if (extension == null || extension.isBlank()) {
                throw new InvalidFileException("Could not determine file extension.");
            }

            extension = extension.toLowerCase();
            String baseName = UUID.randomUUID().toString();
            String mainFilename = baseName + "." + extension;
            String thumbnailFilename = baseName + "_thumb." + extension;

            // Save original image
            String storedPath = fileStorageService.store(file, uploadDir, mainFilename);

            // Generate and store thumbnail
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage thumbnail = Thumbnails.of(originalImage)
                    .size(thumbnailWidth, thumbnailHeight)
                    .asBufferedImage();

            ByteArrayOutputStream thumbOutput = new ByteArrayOutputStream();
            if (!ImageIO.write(thumbnail, extension, thumbOutput)) {
                throw new FileProcessingException("Unsupported image format for thumbnail: " + extension);
            }

            byte[] thumbBytes = thumbOutput.toByteArray();
            String thumbPath = fileStorageService.store(
                    new ByteArrayInputStream(thumbBytes),
                    thumbBytes.length,
                    uploadDir,
                    thumbnailFilename,
                    file.getContentType()
            );

            // Update employee profile picture paths
            employee.setProfilePicturePath(storedPath);
            employee.setProfilePictureThumbnailPath(thumbPath);
            employeeRepository.save(employee);

            return storedPath;
        } catch (IOException e) {
            throw new FileProcessingException("Failed to process profile picture", e);
        }
    }

    @Transactional
    public void deleteProfilePicture(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (employee.getProfilePicturePath() != null) {
            fileStorageService.delete(employee.getProfilePicturePath());
            employee.setProfilePicturePath(null);
        }
        if (employee.getProfilePictureThumbnailPath() != null) {
            fileStorageService.delete(employee.getProfilePictureThumbnailPath());
            employee.setProfilePictureThumbnailPath(null);
        }

        employeeRepository.save(employee);
    }

    public Resource getProfilePicture(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (employee.getProfilePicturePath() == null) {
            throw new ResourceNotFoundException("Profile picture not found for employee ID: " + employeeId);
        }

        return fileStorageService.loadAsResource(employee.getProfilePicturePath());
    }

    public Resource getProfilePictureThumbnail(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (employee.getProfilePictureThumbnailPath() == null) {
            throw new ResourceNotFoundException("Profile picture thumbnail not found for employee ID: " + employeeId);
        }

        return fileStorageService.loadAsResource(employee.getProfilePictureThumbnailPath());
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployeeProfilePicture(Long employeeId, MultipartFile file) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        String filePath = this.uploadProfilePicture(employeeId, file);

        employee.setProfilePicturePath(filePath);
        Employee updated = employeeRepository.save(employee);
        return employeeConverter.convertToDTO(updated);
    }
}
