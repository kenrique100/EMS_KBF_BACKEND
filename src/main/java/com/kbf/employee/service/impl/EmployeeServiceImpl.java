package com.kbf.employee.service.impl;

import com.kbf.employee.dto.*;
import com.kbf.employee.exception.*;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import com.kbf.employee.security.UserPrincipal;
import com.kbf.employee.service.EmployeeService;
import com.kbf.employee.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto, MultipartFile profilePicture, MultipartFile document) {
        validateEmployeeCreation(dto);

        String profilePath = storeFile(profilePicture, "profile");
        String docPath = storeFile(document, "document");

        try {
            Employee employee = buildEmployeeFromDTO(dto);
            employee.setProfilePicturePath(profilePath);
            employee.setDocumentPath(docPath);

            setDefaultRole(employee);
            Employee savedEmployee = employeeRepository.save(employee);

            log.info("Created employee ID {} with profile: {}, document: {}",
                    savedEmployee.getId(), profilePath, docPath);

            return convertToDTO(savedEmployee);
        } catch (Exception e) {
            if (profilePath != null) fileStorageService.delete(profilePath);
            if (docPath != null) fileStorageService.delete(docPath);
            throw new RuntimeException("Failed to create employee: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadFile(String filePath) {
        return fileStorageService.loadAsResource(filePath);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO dto, MultipartFile profilePicture, MultipartFile document) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        validateEmployeeUpdate(employee, dto);

        // Store new files first
        String newProfilePath = null;
        String newDocPath = null;

        try {
            if (profilePicture != null && !profilePicture.isEmpty()) {
                newProfilePath = storeFile(profilePicture, "profile");
            }

            if (document != null && !document.isEmpty()) {
                newDocPath = storeFile(document, "document");
            }

            // Only delete old files after new files are successfully stored
            if (newProfilePath != null) {
                safeDeleteFile(employee.getProfilePicturePath());
                employee.setProfilePicturePath(newProfilePath);
            }

            if (newDocPath != null) {
                safeDeleteFile(employee.getDocumentPath());
                employee.setDocumentPath(newDocPath);
            }

            updateEmployeeFields(employee, dto);
            Employee updated = employeeRepository.save(employee);
            log.info("Updated employee ID {} with profile: {}, document: {}",
                    id, newProfilePath, newDocPath);
            return convertToDTO(updated);
        } catch (Exception e) {
            // Cleanup new files if update fails
            if (newProfilePath != null) safeDeleteFile(newProfilePath);
            if (newDocPath != null) safeDeleteFile(newDocPath);
            throw new RuntimeException("Failed to update employee: " + e.getMessage(), e);
        }
    }

    private void safeDeleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isBlank()) {
                fileStorageService.delete(filePath);
            }
        } catch (Exception e) {
            log.warn("Failed to delete file {}: {}", filePath, e.getMessage());
            // Continue with update even if file deletion fails
        }
    }


    @Override
    public EmployeeProfileDTO getEmployeeProfile(Long id) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!userPrincipal.isAdmin() && !id.equals(userPrincipal.getId())) {
            throw new AccessDeniedException("You are not authorized to access this profile");
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        return EmployeeProfileDTO.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .name(employee.getName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .dateOfEmployment(employee.getDateOfEmployment())
                .status(employee.getStatus())
                .profilePicturePath(employee.getProfilePicturePath())
                .documentPath(employee.getDocumentPath())
                .createdAt(employee.getCreatedAt().toLocalDate())
                .updatedAt(employee.getUpdatedAt().toLocalDate())
                .build();
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return convertToDTO(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        if (employee.getProfilePicturePath() != null) {
            fileStorageService.delete(employee.getProfilePicturePath());
        }
        if (employee.getDocumentPath() != null) {
            fileStorageService.delete(employee.getDocumentPath());
        }

        employeeRepository.delete(employee);
        log.info("Deleted employee ID: {}", id);
    }

    private void validateEmployeeCreation(EmployeeDTO dto) {
        if (employeeRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
    }

    private void validateEmployeeUpdate(Employee employee, EmployeeUpdateDTO dto) {
        if (dto.getUsername() != null && !employee.getUsername().equals(dto.getUsername())) {
            if (employeeRepository.existsByUsername(dto.getUsername())) {
                throw new DuplicateResourceException("Username already exists");
            }
        }

        if (dto.getEmail() != null && !employee.getEmail().equals(dto.getEmail())) {
            if (employeeRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
        }
    }

    private Employee buildEmployeeFromDTO(EmployeeDTO dto) {
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

    private void setDefaultRole(Employee employee) {
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default role not found"));
        employee.setRoles(Set.of(userRole));
    }

    private void updateEmployeeFields(Employee employee, EmployeeUpdateDTO dto) {
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

    private String storeFile(MultipartFile file, String fileType) {
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

    private EmployeeDTO convertToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .name(employee.getName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .dateOfEmployment(employee.getDateOfEmployment())
                .status(employee.getStatus())
                .profilePicturePath(employee.getProfilePicturePath())
                .documentPath(employee.getDocumentPath())
                .build();
    }
}