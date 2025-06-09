package com.kbf.employee.service;

import com.kbf.employee.dto.EmployeeDTO;
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

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        if (employeeRepository.existsByUsername(employeeDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Employee employee = new Employee();
        employee.setUsername(employeeDTO.getUsername());
        employee.setName(employeeDTO.getName());
        employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        employee.setDateOfEmployment(employeeDTO.getDateOfEmployment());
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);

        // Assign default role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        employee.setRoles(Set.of(userRole));

        // Handle file uploads
        if (employeeDTO.getProfilePicture() != null && !employeeDTO.getProfilePicture().isEmpty()) {
            String filename = fileStorageService.store(employeeDTO.getProfilePicture(), "profiles");
            employee.setProfilePicturePath(filename);
        }

        if (employeeDTO.getDocument() != null && !employeeDTO.getDocument().isEmpty()) {
            String filename = fileStorageService.store(employeeDTO.getDocument(), "documents");
            employee.setDocumentPath(filename);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return convertToDTO(employee);
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (!employee.getUsername().equals(employeeDTO.getUsername()) &&
                employeeRepository.existsByUsername(employeeDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        employee.setUsername(employeeDTO.getUsername());
        employee.setName(employeeDTO.getName());

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        }

        employee.setDateOfEmployment(employeeDTO.getDateOfEmployment());

        if (employeeDTO.getStatus() != null) {
            employee.setStatus(employeeDTO.getStatus());
        }

        // Handle file uploads
        if (employeeDTO.getProfilePicture() != null && !employeeDTO.getProfilePicture().isEmpty()) {
            String filename = fileStorageService.store(employeeDTO.getProfilePicture(), "profiles");
            if (employee.getProfilePicturePath() != null) {
                fileStorageService.delete(employee.getProfilePicturePath(), "profiles");
            }
            employee.setProfilePicturePath(filename);
        }

        if (employeeDTO.getDocument() != null && !employeeDTO.getDocument().isEmpty()) {
            String filename = fileStorageService.store(employeeDTO.getDocument(), "documents");
            if (employee.getDocumentPath() != null) {
                fileStorageService.delete(employee.getDocumentPath(), "documents");
            }
            employee.setDocumentPath(filename);
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Delete associated files
        if (employee.getProfilePicturePath() != null) {
            fileStorageService.delete(employee.getProfilePicturePath(), "profiles");
        }
        if (employee.getDocumentPath() != null) {
            fileStorageService.delete(employee.getDocumentPath(), "documents");
        }

        employeeRepository.delete(employee);
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setUsername(employee.getUsername());
        dto.setName(employee.getName());
        dto.setDateOfEmployment(employee.getDateOfEmployment());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}