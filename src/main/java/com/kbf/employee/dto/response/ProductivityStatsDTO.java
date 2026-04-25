package com.kbf.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Productivity Statistics Data Transfer Object")
public class ProductivityStatsDTO {
    private Double totalHoursWorked;
    private Double dailyAverage;
    private Integer workingDays;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private Double productivityPercentage;
}