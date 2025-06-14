package com.kbf.employee.dto;

import com.kbf.employee.model.Department;
import com.kbf.employee.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfileDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private Department department;
    private LocalDate dateOfEmployment;
    private Employee.EmployeeStatus status;
    private String profilePicturePath;
    private String documentPath;
    private List<SalaryPaymentDTO> salaryPayments;
    private List<TaskDTO> tasks;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
