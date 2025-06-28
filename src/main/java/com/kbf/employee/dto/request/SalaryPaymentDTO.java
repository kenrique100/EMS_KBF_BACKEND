package com.kbf.employee.dto.request;

import com.kbf.employee.model.SalaryPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Salary Payment Data Transfer Object")
public class SalaryPaymentDTO {
    @Schema(description = "Payment ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Payment amount", example = "2500.00")
    private BigDecimal amount;

    @PastOrPresent
    @Schema(description = "Date when payment was made", example = "2023-06-30")
    private LocalDate paymentDate;

    @Schema(description = "ID of the employee receiving this payment", example = "1")
    private Long employeeId;

    @Schema(description = "Employee name", example = "John Doe", accessMode = Schema.AccessMode.READ_ONLY)
    private String employeeName;

    @Schema(description = "Payment status", example = "PROCESSED", accessMode = Schema.AccessMode.READ_ONLY)
    private SalaryPayment.PaymentStatus status;

    @Schema(description = "Unique payment reference", example = "PAY-2023-06-001")
    private String paymentReference;

    @Schema(description = "Creation timestamp", example = "2023-06-30T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}