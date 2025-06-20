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

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Employee Data Transfer Object")
public class EmployeeDTO {
    @Schema(description = "Employee ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Schema(description = "Unique username for the employee", example = "john.farmer")
    private String username;

    @NotBlank
    @Schema(description = "Full name of the employee", example = "John Farmer")
    private String name;

    @NotBlank
    @Schema(description = "Password for the employee account", example = "securePassword123")
    private String password;

    @Email
    @NotBlank
    @Schema(description = "Email address of the employee", example = "john.farmer@agro.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s-]{10,}$")
    @Schema(description = "Phone number of the employee", example = "+1234567890")
    private String phoneNumber;

    @NotNull
    @Schema(description = "Department of the employee", example = "POULTRY",
            allowableValues = {"FISHERY", "POULTRY", "RABBITRY", "CONSTRUCTION",
                    "CROPS", "LIVESTOCK", "DAIRY", "AGRO_FORESTRY",
                    "IRRIGATION", "FARM_MANAGEMENT",
                    "AGRICULTURAL_ENGINEERING", "FOOD_PROCESSING"})
    private Department department;

    @NotNull
    @PastOrPresent
    @Schema(description = "Date when employee was hired", example = "2023-01-15")
    private LocalDate dateOfEmployment;

    @Schema(description = "Employee status", example = "ACTIVE", accessMode = Schema.AccessMode.READ_ONLY)
    private Employee.EmployeeStatus status;
}