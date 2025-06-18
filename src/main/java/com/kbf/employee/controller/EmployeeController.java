package com.kbf.employee.controller;

import com.kbf.employee.dto.*;
import com.kbf.employee.exception.InvalidFileException;
import com.kbf.employee.service.EmployeeService;
import com.kbf.employee.service.FileStorageService;
import com.kbf.employee.util.FileValidationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "Employee", description = "Employee Management API")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "Create a new employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Employee already exists"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(
            @Valid @RequestPart("employee") EmployeeDTO employeeDTO,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "document", required = false) MultipartFile document) {

        try {
            if (profilePicture != null && !profilePicture.isEmpty()) {
                FileValidationUtil.validateImageFile(profilePicture);
            }
            if (document != null && !document.isEmpty()) {
                FileValidationUtil.validateDocumentFile(document);
            }

            EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO, profilePicture, document);
            return ResponseEntity.ok(createdEmployee);
        } catch (InvalidFileException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    @Operation(summary = "Get all employees")
    @ApiResponse(responseCode = "200", description = "List of all employees")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Operation(summary = "Get employee by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "Update employee details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestPart("employee") EmployeeUpdateDTO employeeDTO,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "document", required = false) MultipartFile document) {

        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDTO, profilePicture, document));
    }

    @Operation(summary = "Update employee profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid file"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type")
    })
    @PutMapping(value = "/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProfilePicture(
            @PathVariable Long id,
            @RequestPart("profilePicture") MultipartFile profilePicture) {

        try {
            FileValidationUtil.validateImageFile(profilePicture);
            EmployeeDTO updatedEmployee = employeeService.updateProfilePicture(id, profilePicture);
            return ResponseEntity.ok(updatedEmployee);
        } catch (InvalidFileException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        } catch (Exception e) {
            log.error("Error updating profile picture: {}", e.getMessage());
            return buildErrorResponse("An error occurred while updating profile picture",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update employee document")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid file"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type")
    })
    @PutMapping(value = "/{id}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDocument(
            @PathVariable Long id,
            @RequestPart("document") MultipartFile document) {

        try {
            FileValidationUtil.validateDocumentFile(document);
            EmployeeDTO updatedEmployee = employeeService.updateDocument(id, document);
            return ResponseEntity.ok(updatedEmployee);
        } catch (InvalidFileException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        } catch (Exception e) {
            log.error("Error updating document: {}", e.getMessage());
            return buildErrorResponse("An error occurred while updating document",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        log.error("Error occurred: {}", message);
        return ResponseEntity.status(status)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message,
                        "path", "/api/employees"
                ));
    }

    @Operation(summary = "Delete an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete employee profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile picture deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found or no profile picture exists")
    })
    @DeleteMapping("/{id}/profile-picture")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfilePicture(@PathVariable Long id) {
        employeeService.deleteProfilePicture(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete employee document")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found or no document exists")
    })
    @DeleteMapping("/{id}/document")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        employeeService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all stored files")
    @GetMapping("/files")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listAllFiles() {
        List<String> fileNames = fileStorageService.loadAll()
                .map(Path::toString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(fileNames);
    }

    @Operation(summary = "Download a file")
    @GetMapping("/files/{subDirectory}/{filename:.+}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllFiles() {
        fileStorageService.deleteAll();
        fileStorageService.init();
        return ResponseEntity.noContent().build();
    }
}