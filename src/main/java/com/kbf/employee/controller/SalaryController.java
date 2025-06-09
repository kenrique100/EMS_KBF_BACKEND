package com.kbf.employee.controller;

import com.kbf.employee.dto.SalaryPaymentDTO;
import com.kbf.employee.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Salary", description = "Salary Payment Management API")
@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @Operation(summary = "Create a new salary payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salary payment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping
    public ResponseEntity<SalaryPaymentDTO> createSalaryPayment(@Valid @RequestBody SalaryPaymentDTO salaryPaymentDTO) {
        return ResponseEntity.ok(salaryService.createSalaryPayment(salaryPaymentDTO));
    }

    @Operation(summary = "Get all salary payments")
    @ApiResponse(responseCode = "200", description = "List of all salary payments")
    @GetMapping
    public ResponseEntity<List<SalaryPaymentDTO>> getAllSalaryPayments() {
        return ResponseEntity.ok(salaryService.getAllSalaryPayments());
    }

    @Operation(summary = "Get all salary payments for an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of salary payments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<SalaryPaymentDTO>> getSalaryPaymentsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(salaryService.getSalaryPaymentsForEmployee(employeeId));
    }

    @Operation(summary = "Get salary payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salary payment found"),
            @ApiResponse(responseCode = "404", description = "Salary payment not found")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<SalaryPaymentDTO> getSalaryPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(salaryService.getSalaryPaymentById(paymentId));
    }

    @Operation(summary = "Delete a salary payment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Salary payment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Salary payment not found")
    })
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deleteSalaryPayment(@PathVariable Long paymentId) {
        salaryService.deleteSalaryPayment(paymentId);
        return ResponseEntity.noContent().build();
    }
}