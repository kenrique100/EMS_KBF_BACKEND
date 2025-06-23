package com.kbf.employee.dto;

import com.kbf.employee.model.Department;
import com.kbf.employee.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data-transfer object used for both creating and returning employee records.
 */
@Data
@Builder
@Schema(description = "Employee Data Transfer Object")
public class EmployeeDTO {

    @Schema(description = "Employee ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    // ─── Credentials & Identity ──────────────────────────────────────────────────
    @NotBlank
    @Schema(description = "Unique username for the employee", example = "john.farmer")
    private String username;

    @NotBlank
    @Schema(description = "Full name of the employee", example = "John Farmer")
    private String name;

    @NotBlank
    @Schema(description = "Password for the employee account", example = "securePassword123",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    // ─── Contact Info ────────────────────────────────────────────────────────────
    @Email
    @NotBlank
    @Schema(description = "Email address of the employee", example = "john.farmer@agro.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s-]{10,}$")
    @Schema(description = "Phone number of the employee", example = "+1234567890")
    private String phoneNumber;

    // ─── Work Details ────────────────────────────────────────────────────────────
    @NotNull
    @Schema(description = "Department of the employee", example = "POULTRY",
            allowableValues = {
                    "FISHERY", "POULTRY", "RABBITRY", "CONSTRUCTION",
                    "CROPS", "LIVESTOCK", "DAIRY", "AGRO_FORESTRY",
                    "IRRIGATION", "FARM_MANAGEMENT",
                    "AGRICULTURAL_ENGINEERING", "FOOD_PROCESSING"
            })
    private Department department;

    @NotNull
    @PastOrPresent
    @Schema(description = "Date when the employee was hired", example = "2023-01-15")
    private LocalDate dateOfEmployment;

    // ─── Status & File Paths ─────────────────────────────────────────────────────
    @Schema(description = "Employee status", example = "ACTIVE",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Employee.EmployeeStatus status;

    @Schema(description = "Path of the stored profile picture file",
            example = "uploads/employee/avatars/12345.png",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String profilePicturePath;

    @Schema(description = "Path of an optional supporting document",
            example = "uploads/employee/docs/12345.pdf",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String documentPath;

    @Schema(description = "total hours worked last 30 days", accessMode = Schema.AccessMode.READ_ONLY)
    private Double totalHoursWorkedLast30Days;

    @Schema(description = "Status expiration timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime statusExpiration;

    @Schema(description = "Suspension duration", accessMode = Schema.AccessMode.READ_ONLY)
    private Duration suspensionDuration;
}
