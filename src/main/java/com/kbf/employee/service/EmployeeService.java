package com.kbf.employee.service;

import com.kbf.employee.dto.EmployeeDTO;
import com.kbf.employee.dto.EmployeeProfileDTO;
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
            throw new IllegalArgumentException("Username already exists");
        }
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
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
                .orElseThrow(() -> new RuntimeException("Default role not found"));
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
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto, MultipartFile profilePicture, MultipartFile document) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        validateEmployeeUpdate(employee, dto);
        updateEmployeeFields(employee, dto);
        handleFileUpdates(employee, profilePicture, document);

        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    private void validateEmployeeUpdate(Employee employee, EmployeeDTO dto) {
        if (!employee.getUsername().equals(dto.getUsername()) &&
                employeeRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (!employee.getEmail().equals(dto.getEmail()) &&
                employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    private void updateEmployeeFields(Employee employee, EmployeeDTO dto) {
        employee.setUsername(dto.getUsername());
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setDepartment(dto.getDepartment());
        employee.setDateOfEmployment(dto.getDateOfEmployment());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }
    }

    private void handleFileUpdates(Employee employee, MultipartFile profilePicture, MultipartFile document) {
        if (profilePicture != null && !profilePicture.isEmpty()) {
            if (employee.getProfilePicturePath() != null) {
                String existingFile = employee.getProfilePicturePath().split("/")[1];
                fileStorageService.delete(existingFile, "profiles");
            }
            String filename = fileStorageService.store(profilePicture, "profiles");
            employee.setProfilePicturePath("profiles/" + filename);
        }

        if (document != null && !document.isEmpty()) {
            if (employee.getDocumentPath() != null) {
                String existingFile = employee.getDocumentPath().split("/")[1];
                fileStorageService.delete(existingFile, "documents");
            }
            String filename = fileStorageService.store(document, "documents");
            employee.setDocumentPath("documents/" + filename);
        }
    }

    // Other methods remain unchanged...
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

        if (employee.getProfilePicturePath() != null) {
            String filename = employee.getProfilePicturePath().split("/")[1];
            fileStorageService.delete(filename, "profiles");
        }

        if (employee.getDocumentPath() != null) {
            String filename = employee.getDocumentPath().split("/")[1];
            fileStorageService.delete(filename, "documents");
        }

        employeeRepository.delete(employee);
    }

    public EmployeeProfileDTO getEmployeeProfile(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return EmployeeProfileDTO.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .profilePictureUrl(employee.getProfilePicturePath())
                .salaryPayments(salaryService.getSalaryPaymentsForEmployee(employeeId))
                .tasks(taskService.getAllTasksForEmployee(employeeId))
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