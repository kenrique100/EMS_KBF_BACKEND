package com.kbf.employee.dto;

import com.kbf.employee.model.Department;
import com.kbf.employee.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Employee Update Data Transfer Object")
public class EmployeeUpdateDTO {
    @Schema(description = "Employee ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Unique username for the employee", example = "john.farmer")
    private String username;

    @Schema(description = "Full name of the employee", example = "John Farmer")
    private String name;

    @Email
    @Schema(description = "Email address of the employee", example = "john.farmer@agro.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s-]{10,}$")
    @Schema(description = "Phone number of the employee", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Department of the employee", example = "POULTRY",
            allowableValues = {"FISHERY", "POULTRY", "RABBITRY", "CONSTRUCTION",
                    "CROPS", "LIVESTOCK", "DAIRY", "FARM_MANAGEMENT",
                    "AGRICULTURAL_ENGINEERING"})
    private Department department;

    @PastOrPresent
    @Schema(description = "Date when employee was hired", example = "2023-01-15")
    private LocalDate dateOfEmployment;

    @Schema(description = "Password for the employee account", example = "securePassword123")
    private String password;

    @Schema(description = "Profile picture file")
    private transient MultipartFile profilePicture;

    @Schema(description = "Document file (CV, certificates, etc.)")
    private transient MultipartFile document;

    @Schema(description = "Employee status", example = "ACTIVE")
    private Employee.EmployeeStatus status;
}