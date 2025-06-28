package com.kbf.employee.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeeInfoDTO {
    private Long id;
    private String name;
    private String department;
    private String position;
    private LocalDate employmentDate;
}
