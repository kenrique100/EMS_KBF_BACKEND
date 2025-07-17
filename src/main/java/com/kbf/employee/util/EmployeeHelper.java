package com.kbf.employee.util;

import com.kbf.employee.dto.request.EmployeeDTO;
import com.kbf.employee.dto.request.EmployeeUpdateDTO;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeHelper {
    private final PasswordEncoder passwordEncoder;

    public Employee buildEmployeeFromDTO(EmployeeDTO dto) {
        return Employee.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .gender(dto.getGender())
                .email(dto.getEmail())
                .nationalId(dto.getNationalId())
                .phoneNumber(dto.getPhoneNumber())
                .department(dto.getDepartment())
                .password(passwordEncoder.encode(dto.getPassword()))
                .dateOfBirth((dto.getDateOfBirth()))
                .dateOfEmployment(dto.getDateOfEmployment())
                .status(Employee.EmployeeStatus.ACTIVE)
                .workingDaysCount(0)
                .totalHoursWorkedLast30Days(0.0)
                .currentPeriodStartDate(LocalDate.now())
                .totalProductiveDays(0)
                .build();
    }

    public void setDefaultRole(Employee employee, Role userRole) {
        employee.setRoles(Set.of(userRole));
    }

    public void updateEmployeeFields(Employee employee, EmployeeUpdateDTO dto) {
        if (dto.getUsername() != null) employee.setUsername(dto.getUsername());
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getGender() != null) employee.setGender(dto.getGender());
        if (dto.getNationalId() != null) employee.setNationalId(dto.getNationalId());
        if (dto.getEmail() != null) employee.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) employee.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getDateOfEmployment() != null) employee.setDateOfEmployment(dto.getDateOfEmployment());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }
}