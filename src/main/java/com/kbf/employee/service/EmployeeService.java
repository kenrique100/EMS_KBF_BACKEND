package com.kbf.employee.service;

import com.kbf.employee.dto.*;
import com.kbf.employee.exception.DuplicateResourceException;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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
        handleFileUploads(employee, profilePicture, document);

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    private void validateEmployeeCreation(EmployeeDTO dto) {
        if (employeeRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
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

    private void handleFileUploads(Employee employee, MultipartFile profilePicture, MultipartFile document) {
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String filename = fileStorageService.store(profilePicture, "profiles");
            employee.setProfilePicturePath("profiles/" + filename);
        }

        if (document != null && !document.isEmpty()) {
            String filename = fileStorageService.store(document, "documents");
            employee.setDocumentPath("documents/" + filename);
        }
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO dto, MultipartFile profilePicture, MultipartFile document) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        validateEmployeeUpdate(employee, dto);
        updateEmployeeFields(employee, dto);
        handleFileUpdates(employee, profilePicture, document);

        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
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

    private void handleFileUpdates(Employee employee, MultipartFile profilePicture, MultipartFile document) {
        if (profilePicture != null && !profilePicture.isEmpty()) {
            deleteExistingFile(employee.getProfilePicturePath());
            String filename = fileStorageService.store(profilePicture, "profiles");
            employee.setProfilePicturePath("profiles/" + filename);
        }

        if (document != null && !document.isEmpty()) {
            deleteExistingFile(employee.getDocumentPath());
            String filename = fileStorageService.store(document, "documents");
            employee.setDocumentPath("documents/" + filename);
        }
    }

    @Transactional
    public EmployeeDTO updateProfilePicture(Long id, MultipartFile profilePicture) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        try {
            // Delete existing file if present
            if (employee.getProfilePicturePath() != null) {
                fileStorageService.delete(
                        employee.getProfilePicturePath().split("/")[1],
                        employee.getProfilePicturePath().split("/")[0]
                );
            }

            // Store new file
            String filename = fileStorageService.store(profilePicture, "profiles");
            employee.setProfilePicturePath("profiles/" + filename);

            return convertToDTO(employeeRepository.save(employee));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update profile picture", e);
        }
    }

    @Transactional
    public EmployeeDTO updateDocument(Long id, MultipartFile document) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        try {
            // Delete existing file if present
            if (employee.getDocumentPath() != null) {
                fileStorageService.delete(
                        employee.getDocumentPath().split("/")[1],
                        employee.getDocumentPath().split("/")[0]
                );
            }

            // Store new file
            String filename = fileStorageService.store(document, "documents");
            employee.setDocumentPath("documents/" + filename);

            return convertToDTO(employeeRepository.save(employee));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update document", e);
        }
    }

    @Transactional
    public void deleteProfilePicture(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        if (employee.getProfilePicturePath() == null) {
            throw new ResourceNotFoundException("No profile picture exists for employee with ID: " + id);
        }

        deleteExistingFile(employee.getProfilePicturePath());
        employee.setProfilePicturePath(null);
        employeeRepository.save(employee);
    }

    @Transactional
    public void deleteDocument(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        if (employee.getDocumentPath() == null) {
            throw new ResourceNotFoundException("No document exists for employee with ID: " + id);
        }

        deleteExistingFile(employee.getDocumentPath());
        employee.setDocumentPath(null);
        employeeRepository.save(employee);
    }

    private void deleteExistingFile(String filePath) {
        if (filePath != null) {
            String[] parts = filePath.split("/");
            if (parts.length == 2) {
                fileStorageService.delete(parts[1], parts[0]);
            }
        }
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

        deleteExistingFile(employee.getProfilePicturePath());
        deleteExistingFile(employee.getDocumentPath());

        employeeRepository.delete(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeProfileDTO getEmployeeProfile(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        List<SalaryPaymentDTO> salaryPayments = salaryService.getSalaryPaymentsForEmployee(id);
        List<TaskDTO> tasks = taskService.getAllTasksForEmployee(id);

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
                .salaryPayments(salaryPayments)
                .tasks(tasks)
                .createdAt(employee.getCreatedAt() != null ? LocalDate.from(employee.getCreatedAt()) : null)
                .updatedAt(employee.getUpdatedAt() != null ? LocalDate.from(employee.getUpdatedAt()) : null)
                .build();
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
}