package com.kbf.employee.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SalaryReceiptDTO {
    private String receiptNumber;
    private LocalDate issueDate;
    private EmployeeInfoDTO employee;
    private SalaryInfoDTO salary;
    private List<TaskProductivityDTO> tasks;
    private ProductivitySummaryDTO productivitySummary;
}
