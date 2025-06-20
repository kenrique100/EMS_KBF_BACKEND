package com.kbf.employee.controller;

import com.kbf.employee.dto.SalaryPaymentDTO;
import com.kbf.employee.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Salary", description = "Salary Payment Management API")
@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SalaryController {

    private final SalaryService salaryService;

    @Operation(summary = "Create a new salary payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salary payment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalaryPaymentDTO> createSalaryPayment(@Valid @RequestBody SalaryPaymentDTO salaryPaymentDTO) {
        return ResponseEntity.ok(salaryService.createSalaryPayment(salaryPaymentDTO));
    }

    @Operation(summary = "Get all salary payments")
    @ApiResponse(responseCode = "200", description = "List of all salary payments")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<SalaryPaymentDTO>> getAllSalaryPayments() {
        return ResponseEntity.ok(salaryService.getAllSalaryPayments());
    }

    @Operation(summary = "Get all salary payments for an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of salary payments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or @employeeSecurity.isOwner(authentication, #employeeId)")
    public ResponseEntity<List<SalaryPaymentDTO>> getSalaryPaymentsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(salaryService.getSalaryPaymentsForEmployee(employeeId));
    }

    @Operation(summary = "Get salary payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salary payment found"),
            @ApiResponse(responseCode = "404", description = "Salary payment not found")
    })
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<SalaryPaymentDTO> getSalaryPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(salaryService.getSalaryPaymentById(paymentId));
    }

    @Operation(summary = "Delete a salary payment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Salary payment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Salary payment not found")
    })
    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSalaryPayment(@PathVariable Long paymentId) {
        salaryService.deleteSalaryPayment(paymentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a salary payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salary payment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Salary payment not found")
    })
    @PatchMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalaryPaymentDTO> updateSalaryPayment(
            @PathVariable Long paymentId,
            @RequestBody SalaryPaymentDTO salaryPaymentDTO) {
        return ResponseEntity.ok(salaryService.updateSalaryPayment(paymentId, salaryPaymentDTO));
    }

}