package com.kbf.employee.util;

import com.kbf.employee.dto.*;
import com.kbf.employee.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeConverter {
    public EmployeeDTO convertToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .name(employee.getName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .dateOfEmployment(employee.getDateOfEmployment())
                .status(employee.getStatus())
                .statusExpiration(employee.getStatusExpiration())
                .suspensionDuration(employee.getSuspensionDuration())
                .profilePicturePath(employee.getProfilePicturePath())
                .documentPath(employee.getDocumentPath())
                .build();
    }

    public EmployeeProfileDTO convertToProfileDTO(Employee employee) {
        return EmployeeProfileDTO.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .name(employee.getName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .dateOfEmployment(employee.getDateOfEmployment())
                .status(employee.getStatus())
                .statusChangeTimestamp(employee.getStatusChangeTimestamp())
                .statusExpiration(employee.getStatusExpiration())
                .suspensionDuration(employee.getSuspensionDuration())
                .terminationTimestamp(employee.getTerminationTimestamp())
                .profilePicturePath(employee.getProfilePicturePath())
                .documentPath(employee.getDocumentPath())
                .createdAt(employee.getCreatedAt().toLocalDate())
                .updatedAt(employee.getUpdatedAt().toLocalDate())
                .build();
    }
}