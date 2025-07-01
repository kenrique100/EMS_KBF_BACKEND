package com.kbf.employee.service.impl;

import com.kbf.employee.dto.request.*;
import com.kbf.employee.dto.response.EmployeeProfileDTO;
import com.kbf.employee.dto.response.EmployeeStatusHistoryDTO;
import com.kbf.employee.exception.*;
import com.kbf.employee.model.*;
import com.kbf.employee.repository.*;
import com.kbf.employee.security.UserPrincipal;
import com.kbf.employee.service.EmployeeService;
import com.kbf.employee.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusHistoryRepository historyRepository;
    private final RoleRepository roleRepository;
    private final EmployeeValidator employeeValidator;
    private final EmployeeHelper employeeHelper;
    private final EmployeeConverter employeeConverter;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        employeeValidator.validateEmployeeCreation(dto);

        try {
            Employee employee = employeeHelper.buildEmployeeFromDTO(dto);

            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("Default role not found"));
            employeeHelper.setDefaultRole(employee, userRole);

            Employee savedEmployee = employeeRepository.save(employee);
            log.info("Created employee ID {}", savedEmployee.getId());

            createInitialStatusHistory(savedEmployee);
            return employeeConverter.convertToDTO(savedEmployee);
        } catch (Exception e) {
            throw new ServiceOperationException("Failed to create employee", e);
        }
    }

    private void createInitialStatusHistory(Employee employee) {
        historyRepository.save(EmployeeStatusHistory.builder()
                .employee(employee)
                .status(Employee.EmployeeStatus.ACTIVE)
                .startTimestamp(employee.getCreatedAt())
                .build());
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        employeeValidator.validateEmployeeUpdate(employee, dto);
        employeeHelper.updateEmployeeFields(employee, dto);

        Employee updated = employeeRepository.save(employee);
        log.info("Updated employee ID {}", id);
        return employeeConverter.convertToDTO(updated);
    }

    @Override
    public Optional<Employee> getEmployeeByNationalId(String nationalId) {
        return employeeRepository.findByNationalId(nationalId);
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

        closeCurrentStatusHistory(employee);
        updateEmployeeStatusFields(employee, statusUpdateDTO, newStatus);
        createStatusHistory(employee, statusUpdateDTO, newStatus);

        try {
            Employee updated = employeeRepository.save(employee);
            return employeeConverter.convertToDTO(updated);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidOperationException("Database constraint violation during status update");
        } catch (Exception e) {
            log.error("Error updating employee status: {}", e.getMessage(), e);
            throw new ServiceOperationException("Failed to update employee status", e);
        }
    }

    private void closeCurrentStatusHistory(Employee employee) {
        historyRepository.findByEmployeeIdAndEndTimestampIsNull(employee.getId())
                .ifPresent(history -> {
                    history.setEndTimestamp(LocalDateTime.now());
                    history.setActualDuration(Duration.between(
                            history.getStartTimestamp(),
                            LocalDateTime.now()
                    ));
                    historyRepository.save(history);
                });
    }

    private void updateEmployeeStatusFields(Employee employee, EmployeeStatusUpdateDTO statusUpdateDTO,
                                            Employee.EmployeeStatus newStatus) {
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
                employeeValidator.validateSuspensionDuration(statusUpdateDTO.getSuspensionDuration());
                employee.setSuspensionDuration(statusUpdateDTO.getSuspensionDuration());
                employee.setStatusExpiration(LocalDateTime.now().plus(statusUpdateDTO.getSuspensionDuration()));
                break;
            case TERMINATED:
                employee.setTerminationTimestamp(LocalDateTime.now());
                break;
        }
        employee.setStatus(newStatus);
    }

    private void createStatusHistory(Employee employee, EmployeeStatusUpdateDTO statusUpdateDTO,
                                     Employee.EmployeeStatus newStatus) {
        EmployeeStatusHistory history = EmployeeStatusHistory.builder()
                .employee(employee)
                .status(newStatus)
                .startTimestamp(LocalDateTime.now())
                .build();

        switch (newStatus) {
            case SUSPENDED:
                history.setAllocatedDuration(statusUpdateDTO.getSuspensionDuration());
                history.setExpectedEndTimestamp(LocalDateTime.now().plus(statusUpdateDTO.getSuspensionDuration()));
                break;
            case ON_LEAVE:
                Duration allocated = Duration.between(
                        statusUpdateDTO.getLeaveStartDate().atStartOfDay(),
                        statusUpdateDTO.getExpectedReturnDate().atStartOfDay()
                );
                history.setAllocatedDuration(allocated);
                history.setExpectedEndTimestamp(statusUpdateDTO.getExpectedReturnDate().atStartOfDay());
                break;
        }
        historyRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeProfileDTO getEmployeeProfile(Long id) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!userPrincipal.isAdmin() && !id.equals(userPrincipal.getId())) {
            throw new AccessDeniedException("Unauthorized profile access attempt");
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        EmployeeProfileDTO profile = employeeConverter.convertToProfileDTO(employee);
        profile.setStatusHistory(getEmployeeStatusHistory(id));

        // Add salary payments
        List<SalaryPaymentDTO> salaries = salaryPaymentRepository.findByEmployee(employee).stream()
                .map(employeeConverter::convertToSalaryDTO)
                .collect(Collectors.toList());
        profile.setSalaryPayments(salaries);

        // Add assigned tasks
        List<TaskDTO> tasks = taskRepository.findByEmployee(employee).stream()
                .map(employeeConverter::convertToTaskDTO)
                .collect(Collectors.toList());
        profile.setTasks(tasks);

        return profile;
    }

    @Override
    public List<EmployeeStatusHistoryDTO> getEmployeeStatusHistory(Long employeeId) {
        return historyRepository.findByEmployeeIdOrderByStartTimestampDesc(employeeId)
                .stream()
                .map(employeeConverter::convertToHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(employeeConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        return employeeConverter.convertToDTO(employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id)));
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        historyRepository.deleteByEmployeeId(id);
        employeeRepository.delete(employee);
        log.info("Deleted employee ID: {}", id);
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoProcessEmployeeStatuses() {
        LocalDateTime now = LocalDateTime.now();
        reactivateExpiredEmployees(now);
        purgeTerminatedEmployees(now);
    }

    private void reactivateExpiredEmployees(LocalDateTime now) {
        List<Employee> toReactivate = employeeRepository
                .findByStatusExpirationBeforeAndStatusIn(
                        now,
                        List.of(Employee.EmployeeStatus.ON_LEAVE, Employee.EmployeeStatus.SUSPENDED)
                );

        toReactivate.forEach(employee -> {
            closeCurrentStatusHistory(employee);
            employee.setStatus(Employee.EmployeeStatus.ACTIVE);
            employee.setStatusExpiration(null);
            employee.setSuspensionDuration(null);

            historyRepository.save(EmployeeStatusHistory.builder()
                    .employee(employee)
                    .status(Employee.EmployeeStatus.ACTIVE)
                    .startTimestamp(now)
                    .build());

            log.info("Auto-reactivated employee: {}", employee.getId());
        });
        employeeRepository.saveAll(toReactivate);
    }

    private void purgeTerminatedEmployees(LocalDateTime now) {
        LocalDateTime cutoff = now.minusHours(72);
        employeeRepository.findByStatusAndTerminationTimestampBefore(
                Employee.EmployeeStatus.TERMINATED,
                cutoff
        ).forEach(employee -> {
            try {
                historyRepository.deleteByEmployeeId(employee.getId());
                employeeRepository.delete(employee);
                log.info("Purged terminated employee: {}", employee.getId());
            } catch (Exception e) {
                log.error("Purge failed for employee {}: {}", employee.getId(), e.getMessage());
            }
        });
    }
}