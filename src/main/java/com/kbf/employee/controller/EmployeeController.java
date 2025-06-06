package com.kbf.employee.controller;

import com.kbf.employee.dto.EmployeeDTO;
import com.kbf.employee.service.EmployeeService;
import com.kbf.employee.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Employee", description = "Employee Management API")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "Create a new employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Valid @RequestPart("employee") EmployeeDTO employeeDTO,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "document", required = false) MultipartFile document) {

        employeeDTO.setProfilePicture(profilePicture);
        employeeDTO.setDocument(document);
        return ResponseEntity.ok(employeeService.createEmployee(employeeDTO));
    }

    @Operation(summary = "Get all employees")
    @ApiResponse(responseCode = "200", description = "List of all employees")
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Operation(summary = "Get employee by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "Update employee details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute EmployeeDTO employeeDTO) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDTO));
    }

    @Operation(summary = "Delete an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all stored files")
    @GetMapping("/files")
    public ResponseEntity<List<String>> listAllFiles() {
        List<String> fileNames = fileStorageService.loadAll()
                .map(Path::toString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(fileNames);
    }

    @Operation(summary = "Download a file")
    @GetMapping("/files/{subDirectory}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String subDirectory,
            @PathVariable String filename) {

        Resource resource = fileStorageService.loadAsResource(filename, subDirectory);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Delete all files")
    @DeleteMapping("/files")
    public ResponseEntity<Void> deleteAllFiles() {
        fileStorageService.deleteAll();
        fileStorageService.init();
        return ResponseEntity.noContent().build();
    }
}
