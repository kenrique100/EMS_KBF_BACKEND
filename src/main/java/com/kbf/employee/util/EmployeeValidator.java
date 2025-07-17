package com.kbf.employee.util;

import com.kbf.employee.dto.request.EmployeeDTO;
import com.kbf.employee.dto.request.EmployeeStatusUpdateDTO;
import com.kbf.employee.dto.request.EmployeeUpdateDTO;
import com.kbf.employee.exception.*;
import com.kbf.employee.model.enums.Department;
import com.kbf.employee.model.Employee;
import com.kbf.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class EmployeeValidator {
    private final EmployeeRepository employeeRepository;

    public void validateEmployeeCreation(EmployeeDTO dto) {
        if (dto == null) {
            throw new InvalidRequestException("Employee data cannot be null");
        }

        // Check for existing unique fields
        if (employeeRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (employeeRepository.existsByNationalId(dto.getNationalId())) {
            throw new DuplicateResourceException("National ID already exists");
        }

        // Validate department
        if (dto.getDepartment() == null) {
            throw new InvalidRequestException("Department is required");
        }

        // Validate date of employment
        if (dto.getDateOfEmployment() == null) {
            throw new InvalidRequestException("Date of employment is required");
        }
        if (dto.getDateOfEmployment().isAfter(LocalDate.now())) {
            throw new InvalidRequestException("Date of employment cannot be in the future");
        }

        if (dto.getDateOfBirth() == null) {
            throw new InvalidRequestException("Date of birth is required");
        }
    }

    public void validateEmployeeUpdate(Employee employee, EmployeeUpdateDTO dto) {
        if (employee == null || dto == null) {
            throw new InvalidRequestException("Employee and update data cannot be null");
        }

        // Check for existing unique fields if they're being changed
        if (dto.getUsername() != null && !dto.getUsername().equals(employee.getUsername())) {
            if (employeeRepository.existsByUsername(dto.getUsername())) {
                throw new DuplicateResourceException("Username already exists");
            }
        }

        if (dto.getDateOfBirth() == null) {
            throw new InvalidRequestException("Date of birth is required");
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
        }

        if (dto.getNationalId() != null && !dto.getNationalId().equals(employee.getNationalId())) {
            if (employeeRepository.existsByNationalId(dto.getNationalId())) {
                throw new DuplicateResourceException("National ID already exists");
            }
        }

        // Rest of the validation remains the same...
        if (dto.getDepartment() != null && dto.getDepartment() == Department.FARM_MANAGEMENT
                && employee.getStatus() == Employee.EmployeeStatus.ON_LEAVE) {
            throw new InvalidOperationException("Cannot change department to FARM_MANAGEMENT while on leave");
        }

        if (dto.getDateOfEmployment() != null && dto.getDateOfEmployment().isAfter(LocalDate.now())) {
            throw new InvalidRequestException("Date of employment cannot be in the future");
        }
    }

    public void validateStatusTransition(Employee employee, Employee.EmployeeStatus newStatus) {
        if (employee.getStatus() == Employee.EmployeeStatus.TERMINATED) {
            throw new InvalidOperationException("Terminated employees cannot be modified");
        }
        if (newStatus == Employee.EmployeeStatus.ON_LEAVE &&
                employee.getDepartment() == Department.FARM_MANAGEMENT) {
            throw new InvalidOperationException("Farm managers cannot take leave");
        }
    }

    public void validateLeaveDates(EmployeeStatusUpdateDTO dto) {
        if (dto.getLeaveStartDate() == null || dto.getExpectedReturnDate() == null) {
            throw new InvalidRequestException("Leave dates are required");
        }
        if (dto.getExpectedReturnDate().isBefore(dto.getLeaveStartDate())) {
            throw new InvalidRequestException("Return date must be after start date");
        }
        if (dto.getExpectedReturnDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Return date cannot be in the past");
        }
    }

    public void validateSuspensionDuration(Duration duration) {
        if (duration == null) {
            throw new InvalidRequestException("Suspension duration is required");
        }
        if (duration.isNegative() || duration.isZero()) {
            throw new InvalidRequestException("Suspension duration must be positive");
        }
        if (duration.toDays() > 30) {
            throw new InvalidRequestException("Suspension cannot exceed 30 days");
        }
    }
}