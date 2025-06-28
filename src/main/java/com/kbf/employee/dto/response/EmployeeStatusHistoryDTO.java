package com.kbf.employee.dto.response;

import com.kbf.employee.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeStatusHistoryDTO {
    @Schema(description = "Status", example = "ON_LEAVE")
    private Employee.EmployeeStatus status;

    @Schema(description = "Start timestamp", example = "2023-06-15T14:30:00")
    private LocalDateTime startTimestamp;

    @Schema(description = "End timestamp", example = "2023-06-20T09:00:00")
    private LocalDateTime endTimestamp;

    @Schema(description = "Allocated duration", example = "PT120H")
    private Duration allocatedDuration;

    @Schema(description = "Actual duration", example = "PT115H")
    private Duration actualDuration;

    @Schema(description = "Expected end timestamp", example = "2023-06-20T09:00:00")
    private LocalDateTime expectedEndTimestamp;
}