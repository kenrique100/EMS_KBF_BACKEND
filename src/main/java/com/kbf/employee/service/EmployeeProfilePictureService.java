package com.kbf.employee.service;

import com.kbf.employee.dto.request.EmployeeDTO;
import org.springframework.core.io.Resource;

import org.springframework.web.multipart.MultipartFile;

public interface EmployeeProfilePictureService {
    String uploadProfilePicture(Long employeeId, MultipartFile file);
    void deleteProfilePicture(Long employeeId);
    Resource getProfilePicture(Long employeeId);
    Resource getProfilePictureThumbnail(Long employeeId);
    EmployeeDTO updateEmployeeProfilePicture(Long employeeId, MultipartFile file);
}
