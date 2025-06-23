package com.kbf.employee.service;

import com.kbf.employee.dto.*;
import com.kbf.employee.model.Employee;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO, MultipartFile profilePicture, MultipartFile document);
    EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO employeeDTO, MultipartFile profilePicture, MultipartFile document);
    EmployeeDTO updateEmployeeStatus(Long id, EmployeeStatusUpdateDTO statusUpdateDTO);
    EmployeeProfileDTO getEmployeeProfile(Long id);
    List<EmployeeDTO> getAllEmployees();
    EmployeeDTO getEmployeeById(Long id);
    void deleteEmployee(Long id);
    Resource loadFile(String filePath);
    void autoProcessEmployeeStatuses();
    List<EmployeeStatusHistoryDTO> getEmployeeStatusHistory(Long employeeId);
    EmployeeProfileDTO updateProfilePicture(Long employeeId, MultipartFile file);
    Optional<Employee> getEmployeeByFilePath(String filePath);
    EmployeeDTO updateProfilePictureAsDTO(Long employeeId, MultipartFile file);
}