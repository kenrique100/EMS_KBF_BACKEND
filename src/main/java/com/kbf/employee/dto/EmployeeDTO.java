package com.kbf.employee.dto;

import com.kbf.employee.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Schema(description = "Employee Data Transfer Object")
public class EmployeeDTO {
    @Schema(description = "Employee ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Schema(description = "Unique username for the employee", example = "john.doe")
    private String username;

    @NotBlank
    @Schema(description = "Full name of the employee", example = "John Doe")
    private String name;

    @NotBlank
    @Schema(description = "Password for the employee account", example = "securePassword123")
    private String password;

    @NotNull
    @PastOrPresent
    @Schema(description = "Date when employee was hired", example = "2023-01-15")
    private LocalDate dateOfEmployment;

    @Schema(description = "Profile picture file")
    private MultipartFile profilePicture;

    @Schema(description = "Document file (CV, certificates, etc.)")
    private MultipartFile document;

    @Schema(description = "Employee status", example = "ACTIVE", accessMode = Schema.AccessMode.READ_ONLY)
    private Employee.EmployeeStatus status;
}