package com.kbf.employee.service;

import org.springframework.core.io.Resource;

import org.springframework.web.multipart.MultipartFile;

public interface EmployeeProfilePictureService {
    String uploadProfilePicture(Long employeeId, MultipartFile file);
    void removeProfilePicture(Long employeeId);
    Resource getProfilePicture(Long employeeId);
    Resource getProfilePictureThumbnail(Long employeeId);
}
