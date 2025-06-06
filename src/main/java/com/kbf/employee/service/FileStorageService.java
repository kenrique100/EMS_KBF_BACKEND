package com.kbf.employee.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorageService {
    void init();
    String store(MultipartFile file, String subDirectory);
    Stream<Path> loadAll();
    Path load(String filename, String subDirectory);
    Resource loadAsResource(String filename, String subDirectory);
    void deleteAll();
    void delete(String filename, String subDirectory);
}