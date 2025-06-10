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
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Employee employee = Employee.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .department(dto.getDepartment())
                .password(passwordEncoder.encode(dto.getPassword()))
                .dateOfEmployment(dto.getDateOfEmployment())
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found."));
        employee.setRoles(Set.of(userRole));

        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            employee.setProfilePicturePath(fileStorageService.store(dto.getProfilePicture(), "profiles"));
        }

        if (dto.getDocument() != null && !dto.getDocument().isEmpty()) {
            employee.setDocumentPath(fileStorageService.store(dto.getDocument(), "documents"));
        }

        return convertToDTO(employeeRepository.save(employee));
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        return convertToDTO(employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id)));
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        if (!employee.getUsername().equals(dto.getUsername()) &&
                employeeRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        employee.setUsername(dto.getUsername());
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setDepartment(dto.getDepartment());
        employee.setDateOfEmployment(dto.getDateOfEmployment());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }

        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            if (employee.getProfilePicturePath() != null) {
                fileStorageService.delete(employee.getProfilePicturePath(), "profiles");
            }
            employee.setProfilePicturePath(fileStorageService.store(dto.getProfilePicture(), "profiles"));
        }

        if (dto.getDocument() != null && !dto.getDocument().isEmpty()) {
            if (employee.getDocumentPath() != null) {
                fileStorageService.delete(employee.getDocumentPath(), "documents");
            }
            employee.setDocumentPath(fileStorageService.store(dto.getDocument(), "documents"));
        }

        return convertToDTO(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        if (employee.getProfilePicturePath() != null) {
            fileStorageService.delete(employee.getProfilePicturePath(), "profiles");
        }
        if (employee.getDocumentPath() != null) {
            fileStorageService.delete(employee.getDocumentPath(), "documents");
        }

        employeeRepository.delete(employee);
    }

    public EmployeeProfileDTO getEmployeeProfile(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return new EmployeeProfileDTO(
                employee.getId(),
                employee.getUsername(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getDepartment(),
                employee.getProfilePicturePath(),
                salaryService.getSalaryPaymentsForEmployee(employeeId),
                taskService.getAllTasksForEmployee(employeeId)
        );
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setUsername(employee.getUsername());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setPhoneNumber(employee.getPhoneNumber());
        dto.setDepartment(employee.getDepartment());
        dto.setDateOfEmployment(employee.getDateOfEmployment());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}
