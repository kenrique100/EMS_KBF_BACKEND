package com.kbf.employee.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeeInfoDTO {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String nationalId;
    private String department;
    private LocalDate employmentDate;
}
