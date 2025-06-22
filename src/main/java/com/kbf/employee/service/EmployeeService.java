package com.kbf.employee.service;

import com.kbf.employee.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO, MultipartFile profilePicture, MultipartFile document);
    EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO employeeDTO, MultipartFile profilePicture, MultipartFile document);
    EmployeeDTO updateEmployeeStatus(Long id, EmployeeStatusUpdateDTO statusUpdateDTO);
    EmployeeProfileDTO getEmployeeProfile(Long id);
    List<EmployeeDTO> getAllEmployees();
    EmployeeDTO getEmployeeById(Long id);
    void deleteEmployee(Long id);
    Resource loadFile(String filePath);

    @Deprecated
    void autoUpdateEmployeeStatuses(); // Mark as deprecated if keeping for backward compatibility

    void autoProcessEmployeeStatuses();
}