package com.kbf.employee.controller;

import com.kbf.employee.dto.request.SalaryPaymentDTO;
import com.kbf.employee.dto.request.SalaryReceiptDTO;
import com.kbf.employee.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
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

    @Operation(summary = "Get salary receipt for an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salary receipt retrieved"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/salary/{paymentId}/receipt")
    public ResponseEntity<SalaryReceiptDTO> getSalaryReceipt(@PathVariable Long paymentId) {
        SalaryReceiptDTO receipt = salaryService.generateSalaryReceipt(paymentId);
        return ResponseEntity.ok(receipt);
    }


    @Operation(summary = "Download salary receipt PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF receipt downloaded"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/salary/{paymentId}/receipt/pdf")
    public ResponseEntity<byte[]> downloadSalaryReceipt(
            @PathVariable Long paymentId,
            @RequestParam(required = false, defaultValue = "false") boolean preview) {

        byte[] pdfBytes = salaryService.generatePdfReceipt(paymentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        // Get employee name for filename
        SalaryReceiptDTO receipt = salaryService.generateSalaryReceipt(paymentId);
        String employeeName = receipt.getEmployee().getName().replaceAll("[^a-zA-Z0-9]", "_");
        String date = receipt.getSalary().getPaymentDate().format(DateTimeFormatter.BASIC_ISO_DATE);

        if (preview) {
            // For in-browser preview
            headers.setContentDisposition(
                    ContentDisposition.inline()
                            .filename("receipt_" + employeeName + "_" + date + ".pdf")
                            .build()
            );
        } else {
            // For download
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("receipt_" + employeeName + "_" + date + ".pdf")
                            .build()
            );
        }

        // Cache control
        headers.setCacheControl("no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}