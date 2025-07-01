package com.kbf.employee.service;

import com.kbf.employee.dto.request.*;
import com.kbf.employee.dto.response.EmployeeProfileDTO;
import com.kbf.employee.dto.response.EmployeeStatusHistoryDTO;
import com.kbf.employee.model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO employeeDTO);
    EmployeeDTO updateEmployeeStatus(Long id, EmployeeStatusUpdateDTO statusUpdateDTO);
    EmployeeProfileDTO getEmployeeProfile(Long id);
    List<EmployeeDTO> getAllEmployees();
    EmployeeDTO getEmployeeById(Long id);
    void deleteEmployee(Long id);
    void autoProcessEmployeeStatuses();
    List<EmployeeStatusHistoryDTO> getEmployeeStatusHistory(Long employeeId);
    Optional<Employee> getEmployeeByNationalId(String nationalId);
}