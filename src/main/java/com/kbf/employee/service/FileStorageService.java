package com.kbf.employee.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileStorageService {
    void init();
    String store(MultipartFile file, String subDirectory, String filename);
    String store(InputStream inputStream, long size, String subDirectory, String filename, String contentType);
    Resource loadAsResource(String filePath);
    void delete(String filePath);
}