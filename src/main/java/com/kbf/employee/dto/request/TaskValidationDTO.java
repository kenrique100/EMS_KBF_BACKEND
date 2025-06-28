package com.kbf.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "Task Validation Data Transfer Object")
public class TaskValidationDTO {
    @NotNull
    @Schema(description = "Task ID", example = "1")
    private Long taskId;

    @NotNull
    @Schema(description = "Whether to approve the task", example = "true")
    private Boolean approve;
}