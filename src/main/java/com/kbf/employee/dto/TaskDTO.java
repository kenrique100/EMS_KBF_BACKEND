package com.kbf.employee.dto;

import com.kbf.employee.model.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Task Data Transfer Object")
public class TaskDTO {
    @Schema(description = "Task ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Schema(description = "Title of the task", example = "Complete project documentation")
    private String title;

    @Schema(description = "Detailed description of the task", example = "Write user manual for the new feature")
    private String description;

    @NotNull
    @Future
    @Schema(description = "Deadline for the task", example = "2023-12-31T23:59:59")
    private LocalDateTime deadline;

    @NotNull
    @Schema(description = "ID of the employee assigned to this task", example = "1")
    private Long employeeId;

    @Schema(description = "Expected time to complete the task in hours", example = "8")
    private Long expectedHours;

    @Schema(description = "Task status", example = "PENDING", accessMode = Schema.AccessMode.READ_ONLY)
    private Task.TaskStatus status;

    @Schema(description = "Actual time spent on the task in hours", accessMode = Schema.AccessMode.READ_ONLY)
    private Double actualHours;

    @Schema(description = "Start time of the task", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime startTime;

    @Schema(description = "Stop time of the task", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime stopTime;
}