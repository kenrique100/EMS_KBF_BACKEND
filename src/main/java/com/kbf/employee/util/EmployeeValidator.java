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
        if (employeeRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
    }

    public void validateEmployeeUpdate(Employee employee, EmployeeUpdateDTO dto) {
        if (dto.getUsername() != null &&
                !dto.getUsername().equals(employee.getUsername()) &&
                employeeRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (dto.getEmail() != null &&
                !dto.getEmail().equals(employee.getEmail()) &&
                employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
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

    public void validateProductivityData(Employee employee) {
        if (employee.getWorkingDaysCount() < 0) {
            throw new InvalidDataException("Working days count cannot be negative");
        }
        if (employee.getTotalHoursWorkedLast30Days() < 0) {
            throw new InvalidDataException("Total hours worked cannot be negative");
        }
    }
}