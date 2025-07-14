package com.kbf.employee.dto.request;

import com.kbf.employee.model.enums.Department;
import com.kbf.employee.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Employee Update Data Transfer Object")
public class EmployeeUpdateDTO {
    @Schema(description = "Employee ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain alphanumeric characters, dots, underscores and hyphens")
    @Schema(description = "Unique username for the employee", example = "john.farmer")
    private String username;

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name can only contain letters, spaces, apostrophes and hyphens")
    @Schema(description = "Full name of the employee", example = "John Farmer")
    private String name;

    @Size(min = 9, max = 15, message = "National ID must be between 9 and 15 digits")
    @Pattern(regexp = "^[0-9]+$", message = "National ID must contain only digits")
    @Schema(description = "National identity card number", example = "1234567890")
    private String nationalId;

    @Email(message = "Email should be valid")
    @Schema(description = "Email address of the employee", example = "john.farmer@agro.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s-]{10,15}$", message = "Phone number must be between 10-15 digits and may start with +")
    @Schema(description = "Phone number of the employee", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Department of the employee", example = "POULTRY",
            allowableValues = {"FISHERY", "POULTRY", "RABBITRY", "CONSTRUCTION",
                    "CROPS", "LIVESTOCK", "DAIRY", "FARM_MANAGEMENT",
                    "AGRICULTURAL_ENGINEERING"})
    private Department department;

    @PastOrPresent(message = "Date of employment must be in the past or present")
    @Schema(description = "Date when employee was hired", example = "2023-01-15")
    private LocalDate dateOfEmployment;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character")
    @Schema(description = "Password for the employee account", example = "Secure@123",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
}