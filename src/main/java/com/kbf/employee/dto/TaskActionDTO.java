package com.kbf.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Task Action Data Transfer Object")
public class TaskActionDTO {
    @Schema(description = "Task ID", example = "1")
    private Long taskId;

    @Schema(description = "Action to perform (START, STOP, CONTINUE, COMPLETE)", example = "START")
    private String action;
}