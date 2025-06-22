package com.kbf.employee.service.impl;

import com.kbf.employee.dto.*;
import com.kbf.employee.exception.*;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import com.kbf.employee.security.UserPrincipal;
import com.kbf.employee.service.EmployeeService;
import com.kbf.employee.util.EmployeeConverter;
import com.kbf.employee.util.EmployeeHelper;
import com.kbf.employee.util.EmployeeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeValidator employeeValidator;
    private final EmployeeHelper employeeHelper;
    private final EmployeeConverter employeeConverter;

    @Override
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto, MultipartFile profilePicture, MultipartFile document) {
        employeeValidator.validateEmployeeCreation(dto);

        String profilePath = employeeHelper.storeFile(profilePicture, "profile");
        String docPath = employeeHelper.storeFile(document, "document");

        try {
            Employee employee = employeeHelper.buildEmployeeFromDTO(dto);
            employee.setProfilePicturePath(profilePath);
            employee.setDocumentPath(docPath);

            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("Default role not found"));
            employeeHelper.setDefaultRole(employee, userRole);

            Employee savedEmployee = employeeRepository.save(employee);
            log.info("Created employee ID {} with profile: {}, document: {}",
                    savedEmployee.getId(), profilePath, docPath);

            return employeeConverter.convertToDTO(savedEmployee);
        } catch (Exception e) {
            if (profilePath != null) employeeHelper.safeDeleteFile(profilePath);
            if (docPath != null) employeeHelper.safeDeleteFile(docPath);
            throw new RuntimeException("Failed to create employee: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO dto, MultipartFile profilePicture, MultipartFile document) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        employeeValidator.validateEmployeeUpdate(employee, dto);

        String newProfilePath = null;
        String newDocPath = null;

        try {
            if (profilePicture != null && !profilePicture.isEmpty()) {
                newProfilePath = employeeHelper.storeFile(profilePicture, "profile");
            }

            if (document != null && !document.isEmpty()) {
                newDocPath = employeeHelper.storeFile(document, "document");
            }

            if (newProfilePath != null) {
                employeeHelper.safeDeleteFile(employee.getProfilePicturePath());
                employee.setProfilePicturePath(newProfilePath);
            }

            if (newDocPath != null) {
                employeeHelper.safeDeleteFile(employee.getDocumentPath());
                employee.setDocumentPath(newDocPath);
            }

            employeeHelper.updateEmployeeFields(employee, dto);
            Employee updated = employeeRepository.save(employee);
            log.info("Updated employee ID {} with profile: {}, document: {}",
                    id, newProfilePath, newDocPath);
            return employeeConverter.convertToDTO(updated);
        } catch (Exception e) {
            if (newProfilePath != null) employeeHelper.safeDeleteFile(newProfilePath);
            if (newDocPath != null) employeeHelper.safeDeleteFile(newDocPath);
            throw new RuntimeException("Failed to update employee: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployeeStatus(Long id, EmployeeStatusUpdateDTO statusUpdateDTO) {
        if (statusUpdateDTO == null) {
            throw new InvalidRequestException("Status update data cannot be null");
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        Employee.EmployeeStatus newStatus = statusUpdateDTO.getStatus();
        employeeValidator.validateStatusTransition(employee, newStatus);

        // Clear all status-related fields initially
        employee.setStatusExpiration(null);
        employee.setSuspensionDuration(null);
        employee.setTerminationTimestamp(null);
        employee.setStatusChangeTimestamp(LocalDateTime.now());

        switch (newStatus) {
            case ON_LEAVE:
                employeeValidator.validateLeaveDates(statusUpdateDTO);
                employee.setStatusExpiration(statusUpdateDTO.getExpectedReturnDate().atStartOfDay());
                break;

            case SUSPENDED:
                try {
                    Duration duration = statusUpdateDTO.getSuspensionDuration();
                    employeeValidator.validateSuspensionDuration(duration);
                    employee.setSuspensionDuration(duration);
                    employee.setStatusExpiration(LocalDateTime.now().plus(duration));
                } catch (DateTimeParseException e) {
                    throw new InvalidRequestException("Invalid duration format. Use ISO-8601 format (e.g., PT1H30M)");
                }
                break;

            case TERMINATED:
                employee.setTerminationTimestamp(LocalDateTime.now());
                break;
        }

        employee.setStatus(newStatus);

        try {
            Employee updated = employeeRepository.save(employee);
            return employeeConverter.convertToDTO(updated);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidOperationException("Failed to update employee status due to database constraints");
        } catch (Exception e) {
            log.error("Error updating employee status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update employee status", e);
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

        return employeeConverter.convertToProfileDTO(employee);
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(employeeConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Resource loadFile(String filePath) {
        return employeeHelper.loadFile(filePath);
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return employeeConverter.convertToDTO(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        employeeHelper.safeDeleteFile(employee.getProfilePicturePath());
        employeeHelper.safeDeleteFile(employee.getDocumentPath());

        employeeRepository.delete(employee);
        log.info("Deleted employee ID: {}", id);
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoUpdateEmployeeStatuses() {
        autoProcessEmployeeStatuses();
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoProcessEmployeeStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Reactivate employees
        List<Employee> toReactivate = employeeRepository
                .findByStatusExpirationBeforeAndStatusIn(
                        now,
                        List.of(Employee.EmployeeStatus.ON_LEAVE, Employee.EmployeeStatus.SUSPENDED)
                );

        toReactivate.forEach(employee -> {
            employee.setStatus(Employee.EmployeeStatus.ACTIVE);
            employee.setStatusExpiration(null);
            employee.setSuspensionDuration(null);
            log.info("Auto-updated status to ACTIVE for employee: {}", employee.getId());
        });

        // Delete terminated employees
        LocalDateTime seventyTwoHoursAgo = now.minusHours(72);
        List<Employee> toDelete = employeeRepository.findByStatusAndTerminationTimestampBefore(
                Employee.EmployeeStatus.TERMINATED,
                seventyTwoHoursAgo
        );

        toDelete.forEach(employee -> {
            try {
                employeeHelper.safeDeleteFile(employee.getProfilePicturePath());
                employeeHelper.safeDeleteFile(employee.getDocumentPath());
                employeeRepository.delete(employee);
                log.info("Auto-deleted terminated employee: {}", employee.getId());
            } catch (Exception e) {
                log.error("Failed to auto-delete employee {}: {}", employee.getId(), e.getMessage());
            }
        });

        employeeRepository.saveAll(toReactivate);
    }
}