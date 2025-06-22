package com.kbf.employee.util;

import com.kbf.employee.dto.*;
import com.kbf.employee.exception.FileStorageException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeHelper {
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public Employee buildEmployeeFromDTO(EmployeeDTO dto) {
        return Employee.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .department(dto.getDepartment())
                .password(passwordEncoder.encode(dto.getPassword()))
                .dateOfEmployment(dto.getDateOfEmployment())
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();
    }

    public void setDefaultRole(Employee employee, Role userRole) {
        employee.setRoles(Set.of(userRole));
    }

    public void updateEmployeeFields(Employee employee, EmployeeUpdateDTO dto) {
        if (dto.getUsername() != null) {
            employee.setUsername(dto.getUsername());
        }
        if (dto.getName() != null) {
            employee.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            employee.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            employee.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDepartment() != null) {
            employee.setDepartment(dto.getDepartment());
        }
        if (dto.getDateOfEmployment() != null) {
            employee.setDateOfEmployment(dto.getDateOfEmployment());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }
    }

    public String storeFile(MultipartFile file, String fileType) {
        if (file != null && !file.isEmpty()) {
            try {
                String filename = fileStorageService.store(file);
                log.debug("Stored {} file: {}", fileType, filename);
                return filename;
            } catch (Exception e) {
                log.error("Failed to store {} file: {}", fileType, e.getMessage());
                throw new FileStorageException("Failed to store " + fileType + " file");
            }
        }
        return null;
    }


    public void safeDeleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isBlank()) {
                fileStorageService.delete(filePath);
            }
        } catch (Exception e) {
            log.warn("Failed to delete file {}: {}", filePath, e.getMessage());
        }
    }
    public Resource loadFile(String filePath) {
        try {
            return fileStorageService.loadAsResource(filePath);
        } catch (Exception e) {
            log.error("Failed to load file: {}", filePath, e);
            throw new FileStorageException("Failed to load file: " + filePath);
        }
    }
}