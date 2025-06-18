// TaskDTO.java
package com.kbf.employee.dto;

import com.kbf.employee.model.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@Schema(description = "Task Data Transfer Object")
public class TaskDTO {
    @Schema(description = "Task ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Size(max = 100)
    @Schema(description = "Task title", example = "Complete project documentation")
    private String title;

    @Size(max = 500)
    @Schema(description = "Task description", example = "Write detailed documentation for the new feature")
    private String description;

    @FutureOrPresent
    @Schema(description = "Task deadline", example = "2023-12-31")
    private Date deadline;

    @Schema(description = "ID of the employee assigned to this task", example = "1")
    private Long employeeId;

    @Schema(description = "Name of the employee assigned to this task", example = "John Doe", accessMode = Schema.AccessMode.READ_ONLY)
    private String employeeName;

    @Schema(description = "Task status", example = "IN_PROGRESS", accessMode = Schema.AccessMode.READ_ONLY)
    private Task.TaskStatus status;

    @Min(1)
    @Schema(description = "Expected hours to complete the task", example = "8")
    private Integer expectedHours;

    @Schema(description = "Actual hours spent on the task", example = "7.5", accessMode = Schema.AccessMode.READ_ONLY)
    private Double actualHours;

    @Schema(description = "When the task was started", example = "2023-06-15T09:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime startTime;

    @Schema(description = "When the task was stopped", example = "2023-06-15T17:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime stopTime;

    @Schema(description = "Creation timestamp", example = "2023-06-15T08:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2023-06-15T17:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}