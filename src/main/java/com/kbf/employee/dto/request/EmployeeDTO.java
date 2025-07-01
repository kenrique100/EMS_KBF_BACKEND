package com.kbf.employee.dto.request;

import com.kbf.employee.model.enums.Department;
import com.kbf.employee.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @NotBlank(message = "National ID cannot be blank")
    @Size(min = 9, max = 15, message = "National ID must be between 9 and 15 digits")
    @Pattern(regexp = "^[0-9]+$", message = "National ID must contain only digits")
    @Schema(description = "National identity card number", example = "1234567890")
    private String nationalId;


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
                    "AGRICULTURAL_ENGINEERING", "FOOD_PROCESSING", "ADMINISTRATION"
            })
    private Department department;

    @NotNull
    @PastOrPresent
    @Schema(description = "Date when the employee was hired", example = "2023-01-15")
    private LocalDate dateOfEmployment;

    // ─── Status & Metrics ────────────────────────────────────────────────────────
    @Schema(description = "Employee status", example = "ACTIVE",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Employee.EmployeeStatus status;

    @Schema(description = "total hours worked last 30 days", accessMode = Schema.AccessMode.READ_ONLY)
    private Double totalHoursWorkedLast30Days;

    @Schema(description = "Status expiration timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime statusExpiration;

    @Schema(description = "Suspension duration", accessMode = Schema.AccessMode.READ_ONLY)
    private Duration suspensionDuration;

    @Schema(description = "Timestamp when the employee was created", example = "2023-01-15T09:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the employee was last updated", example = "2023-06-27T15:45:00")
    private LocalDateTime updatedAt;
}