package com.kbf.employee.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductivitySummaryDTO {
    private BigDecimal totalExpectedHours;
    private BigDecimal totalActualHours;
    private BigDecimal overallProductivity;
    private Integer workingDays;
}
