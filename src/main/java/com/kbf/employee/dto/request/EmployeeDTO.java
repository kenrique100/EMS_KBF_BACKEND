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
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain alphanumeric characters, dots, underscores and hyphens")
    @Schema(description = "Unique username for the employee", example = "john.farmer")
    private String username;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name can only contain letters, spaces, apostrophes and hyphens")
    @Schema(description = "Full name of the employee", example = "John Farmer")
    private String name;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character")
    @Schema(description = "Password for the employee account", example = "Secure@123",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @NotBlank(message = "National ID cannot be blank")
    @Size(min = 9, max = 15, message = "National ID must be between 9 and 15 digits")
    @Pattern(regexp = "^[0-9]+$", message = "National ID must contain only digits")
    @Schema(description = "National identity card number", example = "1234567890")
    private String nationalId;

    // ─── Contact Info ────────────────────────────────────────────────────────────
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @Schema(description = "Email address of the employee", example = "john.farmer@agro.com")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[0-9\\s-]{10,15}$", message = "Phone number must be between 10-15 digits and may start with +")
    @Schema(description = "Phone number of the employee", example = "+1234567890")
    private String phoneNumber;

    // ─── Work Details ────────────────────────────────────────────────────────────
    @NotNull(message = "Department cannot be null")
    @Schema(description = "Department of the employee", example = "POULTRY",
            allowableValues = {
                    "FISHERY", "POULTRY", "RABBITRY", "CONSTRUCTION",
                    "CROPS", "LIVESTOCK", "DAIRY", "AGRO_FORESTRY",
                    "IRRIGATION", "FARM_MANAGEMENT",
                    "AGRICULTURAL_ENGINEERING", "FOOD_PROCESSING", "ADMINISTRATION"
            })
    private Department department;

    @NotNull(message = "Date of employment cannot be null")
    @PastOrPresent(message = "Date of employment must be in the past or present")
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

    @Schema(description = "Timestamp when the employee was created", example = "2023-01-15T09:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the employee was last updated", example = "2023-06-27T15:45:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}