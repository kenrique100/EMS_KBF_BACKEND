package com.kbf.employee.dto;

import com.kbf.employee.model.Department;
import com.kbf.employee.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee Profile Data Transfer Object")
public class EmployeeProfileDTO {
    @Schema(description = "Employee ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Unique username", example = "john.doe")
    private String username;

    @Schema(description = "Full name", example = "John Doe")
    private String name;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Department", implementation = Department.class)
    private Department department;

    @Schema(description = "Date of employment", example = "2023-01-15")
    private LocalDate dateOfEmployment;

    @Schema(description = "Employee status", implementation = Employee.EmployeeStatus.class)
    private Employee.EmployeeStatus status;

    @Schema(description = "Path to profile picture", example = "profiles/12345_profile.jpg")
    private String profilePicturePath;

    @Schema(description = "Path to documents", example = "documents/12345_cv.pdf")
    private String documentPath;

    @Schema(description = "List of salary payments")
    private List<SalaryPaymentDTO> salaryPayments;

    @Schema(description = "List of assigned tasks")
    private List<TaskDTO> tasks;

    @Schema(description = "Creation date", example = "2023-01-15")
    private LocalDate createdAt;

    @Schema(description = "Last update date", example = "2023-06-01")
    private LocalDate updatedAt;
}