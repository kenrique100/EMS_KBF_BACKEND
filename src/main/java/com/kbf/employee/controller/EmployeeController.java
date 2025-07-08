package com.kbf.employee.controller;

import com.kbf.employee.dto.request.*;
import com.kbf.employee.dto.response.EmployeeProfileDTO;
import com.kbf.employee.dto.response.EmployeeStatusHistoryDTO;
import com.kbf.employee.exception.DuplicateResourceException;
import com.kbf.employee.exception.InvalidRequestException;
import com.kbf.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Employee", description = "Employee Management API")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Create a new employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Employee already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        log.info("Creating employee: {}", employeeDTO.getUsername());

        try {
            EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
            return ResponseEntity.ok(createdEmployee);
        } catch (DuplicateResourceException e) {
            log.error("Duplicate resource error: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.CONFLICT);
        } catch (InvalidRequestException e) {
            log.error("Invalid request error: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error creating employee: {}", e.getMessage(), e);
            return buildErrorResponse("Failed to create employee", HttpStatus.INTERNAL_SERVER_ERROR);
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

    @Operation(summary = "Get employee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee profile retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/profile/{id}")
    public ResponseEntity<EmployeeProfileDTO> getEmployeeProfile(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeProfile(id));
    }

    @Operation(summary = "Partially update employee details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateDTO employeeUpdateDTO) {

        log.info("Received PATCH request for employee ID: {}", id);
        log.debug("Request body: {}", employeeUpdateDTO);

        try {
            EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeUpdateDTO);
            log.info("Successfully updated employee ID: {}", id);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            log.error("Error updating employee ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Update employee status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeStatusUpdateDTO statusUpdateDTO) {

        log.info("Updating status for employee ID: {}", id);
        return ResponseEntity.ok(employeeService.updateEmployeeStatus(id, statusUpdateDTO));
    }

    @Operation(summary = "Delete an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("Deleting employee ID: {}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get employee status history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status history retrieved"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/history/status/{id}")
    public ResponseEntity<List<EmployeeStatusHistoryDTO>> getEmployeeStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeStatusHistory(id));
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
}