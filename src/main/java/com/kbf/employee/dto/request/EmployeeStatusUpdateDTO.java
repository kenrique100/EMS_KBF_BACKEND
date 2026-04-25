package com.kbf.employee.dto.request;

import com.kbf.employee.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

@Data
@Schema(description = "Employee Status Update DTO")
public class EmployeeStatusUpdateDTO {
    @Schema(description = "New employee status", required = true)
    private Employee.EmployeeStatus status;

    @Schema(description = "Leave start date (required for ON_LEAVE)")
    private LocalDate leaveStartDate;

    @Schema(description = "Expected return date (required for ON_LEAVE)")
    private LocalDate expectedReturnDate;

    @Schema(description = "Suspension duration in ISO-8601 format (e.g., PT1H30M)",
            example = "PT1H30M",
            required = false)
    @Pattern(regexp = "^PT(\\d+H)?(\\d+M)?(\\d+S)?$", message = "Must be in ISO-8601 duration format (e.g., PT1H30M)")
    private String suspensionDuration;

    // Helper to convert incoming string to Duration for service layer
    public Duration getSuspensionDuration() {
        return suspensionDuration != null ? Duration.parse(suspensionDuration) : null;
    }
}