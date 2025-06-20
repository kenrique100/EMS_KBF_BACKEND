package com.kbf.employee.service;

import com.kbf.employee.dto.*;
import com.kbf.employee.exception.AccessDeniedException;
import com.kbf.employee.exception.DuplicateResourceException;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import com.kbf.employee.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final SalaryService salaryService;
    private final TaskService taskService;

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto, MultipartFile profilePicture, MultipartFile document) {
        validateEmployeeCreation(dto);
        Employee employee = buildEmployeeFromDTO(dto);
        setDefaultRole(employee);

        try {
            handleFileUpload(profilePicture, "profile", employee::setProfilePicturePath, null);
            handleFileUpload(document, "document", employee::setDocumentPath, null);

            return convertToDTO(employeeRepository.save(employee));
        } catch (Exception e) {
            cleanupFiles(employee);
            throw new RuntimeException("Failed to create employee: " + e.getMessage(), e);
        }
    }


    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO dto, MultipartFile profilePicture, MultipartFile document) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        validateEmployeeUpdate(employee, dto);
        updateEmployeeFields(employee, dto);

        try {
            handleFileUpload(profilePicture, "profile", employee::setProfilePicturePath, employee.getProfilePicturePath());
            handleFileUpload(document, "document", employee::setDocumentPath, employee.getDocumentPath());

            return convertToDTO(employeeRepository.save(employee));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update employee: " + e.getMessage(), e);
        }
    }

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
                .salaryPayments(salaryService.getSalaryPaymentsForEmployee(id))
                .tasks(taskService.getAllTasksForEmployee(id))
                .build();
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return convertToDTO(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        cleanupFiles(employee);
        employeeRepository.delete(employee);
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

    private void handleFileUpload(
            MultipartFile file,
            String fileType,
            Consumer<String> pathSetter,
            String currentPath
    ) {
        if (file != null && !file.isEmpty()) {
            // Delete old file if exists
            if (currentPath != null) {
                String filename = currentPath.substring(currentPath.lastIndexOf("/") + 1);
                fileStorageService.delete(filename, fileType + "s");
            }
            // Save new file
            String filename = fileStorageService.store(file, fileType + "s");
            pathSetter.accept(fileType + "s/" + filename);
        }
    }

    private void cleanupFiles(Employee employee) {
        if (employee.getProfilePicturePath() != null) {
            String filename = employee.getProfilePicturePath().split("/")[1];
            fileStorageService.delete(filename, "profiles");
        }
        if (employee.getDocumentPath() != null) {
            String filename = employee.getDocumentPath().split("/")[1];
            fileStorageService.delete(filename, "documents");
        }
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
                .build();
    }

    private EmployeeProfileDTO convertToProfileDTO(Employee employee) {
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
                .salaryPayments(List.of())
                .tasks(List.of())
                .build();
    }
}