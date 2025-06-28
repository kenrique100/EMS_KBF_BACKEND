package com.kbf.employee.util;

import com.kbf.employee.dto.request.EmployeeDTO;
import com.kbf.employee.dto.request.SalaryPaymentDTO;
import com.kbf.employee.dto.request.TaskDTO;
import com.kbf.employee.dto.response.EmployeeProfileDTO;
import com.kbf.employee.dto.response.EmployeeStatusHistoryDTO;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.EmployeeStatusHistory;
import com.kbf.employee.model.SalaryPayment;
import com.kbf.employee.model.Task;
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
                .totalHoursWorkedLast30Days(employee.getTotalHoursWorkedLast30Days())
                .createdAt(employee.getCreatedAt()) // <-- Ensure this is set
                .updatedAt(employee.getUpdatedAt())
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
                .totalHoursWorkedLast30Days(employee.getTotalHoursWorkedLast30Days())
                .createdAt(employee.getCreatedAt().toLocalDate())
                .updatedAt(employee.getUpdatedAt().toLocalDate())
                .build();
    }

    public EmployeeStatusHistoryDTO convertToHistoryDTO(EmployeeStatusHistory history) {
        return EmployeeStatusHistoryDTO.builder()
                .status(history.getStatus())
                .startTimestamp(history.getStartTimestamp())
                .endTimestamp(history.getEndTimestamp())
                .allocatedDuration(history.getAllocatedDuration())
                .actualDuration(history.getActualDuration())
                .expectedEndTimestamp(history.getExpectedEndTimestamp())
                .build();
    }

    public SalaryPaymentDTO convertToSalaryDTO(SalaryPayment payment) {
        return SalaryPaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .employeeId(payment.getEmployee().getId())
                .employeeName(payment.getEmployee().getName())
                .status(payment.getStatus())
                .paymentReference(payment.getPaymentReference())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public TaskDTO convertToTaskDTO(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .employeeId(task.getEmployee().getId())
                .employeeName(task.getEmployee().getName())
                .status(task.getStatus())
                .expectedHours(task.getExpectedHours())
                .actualHours(task.getActualHours())
                .totalWorkedMinutes(task.getTotalWorkedMinutes())
                .startTime(task.getStartTime())
                .stopTime(task.getStopTime())
                .lastResumeTime(task.getLastResumeTime())
                .isValidated(task.getIsValidated())
                .validationTime(task.getValidationTime())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public EmployeeDTO convertProfileToBasicDTO(EmployeeProfileDTO profile) {
        return EmployeeDTO.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .name(profile.getName())
                .email(profile.getEmail())
                .phoneNumber(profile.getPhoneNumber())
                .department(profile.getDepartment())
                .dateOfEmployment(profile.getDateOfEmployment())
                .status(profile.getStatus())
                .profilePicturePath(profile.getProfilePicturePath())
                .documentPath(profile.getDocumentPath())
                .totalHoursWorkedLast30Days(profile.getTotalHoursWorkedLast30Days())
                .build();
    }
}