package com.kbf.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Productivity Statistics Data Transfer Object")
public class ProductivityStatsDTO {
    @Schema(description = "Total hours worked in current period", example = "120.5")
    private Double totalHoursWorked;

    @Schema(description = "Daily average hours worked", example = "4.02")
    private Double dailyAverage;

    @Schema(description = "Number of working days in period", example = "30")
    private Integer workingDays;

    @Schema(description = "Start date of current period", example = "2023-06-01")
    private LocalDate periodStartDate;

    @Schema(description = "End date of current period", example = "2023-06-30")
    private LocalDate periodEndDate;

    @Schema(description = "Percentage of expected productivity", example = "85.5")
    private Double productivityPercentage;
}