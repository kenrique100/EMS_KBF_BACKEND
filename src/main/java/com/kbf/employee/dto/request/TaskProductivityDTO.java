package com.kbf.employee.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TaskProductivityDTO {
    private String title;
    private BigDecimal expectedHours;
    private BigDecimal actualHours;
    private BigDecimal completionRate;
    private String status;
}