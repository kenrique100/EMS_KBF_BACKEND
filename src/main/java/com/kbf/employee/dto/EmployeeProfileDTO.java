package com.kbf.employee.dto;

import com.kbf.employee.model.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private Department department;
    private String profilePictureUrl;
    private List<SalaryPaymentDTO> salaryPayments;
    private List<TaskDTO> tasks;
}