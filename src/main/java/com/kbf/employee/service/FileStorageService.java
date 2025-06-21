package com.kbf.employee.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    void init();
    String store(MultipartFile file);
    Resource loadAsResource(String filename);
    void delete(String filename);
}