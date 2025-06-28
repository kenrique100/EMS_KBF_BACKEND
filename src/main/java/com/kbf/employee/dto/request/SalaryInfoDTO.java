package com.kbf.employee.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SalaryInfoDTO {
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentReference;
    private String status;
    private String paymentMethod;
}