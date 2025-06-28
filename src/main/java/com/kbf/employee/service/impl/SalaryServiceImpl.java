package com.kbf.employee.service.impl;

import com.kbf.employee.dto.request.*;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.SalaryPayment;
import com.kbf.employee.model.Task;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.SalaryPaymentRepository;
import com.kbf.employee.repository.TaskRepository;
import com.kbf.employee.service.ReceiptGeneratorService;
import com.kbf.employee.service.SalaryService;
import com.kbf.employee.util.EmployeeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final SalaryPaymentRepository salaryPaymentRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final EmployeeConverter employeeConverter;
    private final ReceiptGeneratorService receiptGeneratorService;

    @Override
    @Transactional
    public SalaryPaymentDTO createSalaryPayment(SalaryPaymentDTO salaryPaymentDTO) {
        Employee employee = employeeRepository.findById(salaryPaymentDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + salaryPaymentDTO.getEmployeeId()));

        SalaryPayment payment = SalaryPayment.builder()
                .amount(salaryPaymentDTO.getAmount())
                .paymentDate(salaryPaymentDTO.getPaymentDate())
                .employee(employee)
                .status(SalaryPayment.PaymentStatus.PROCESSED)
                .paymentReference(generatePaymentReference())
                .build();

        SalaryPayment savedPayment = salaryPaymentRepository.save(payment);
        return employeeConverter.convertToSalaryDTO(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryPaymentDTO> getAllSalaryPayments() {
        return salaryPaymentRepository.findAll().stream()
                .map(employeeConverter::convertToSalaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryPaymentDTO> getSalaryPaymentsForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return salaryPaymentRepository.findByEmployee(employee).stream()
                .map(employeeConverter::convertToSalaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryPaymentDTO getSalaryPaymentById(Long paymentId) {
        SalaryPayment payment = salaryPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary payment not found with id: " + paymentId));
        return employeeConverter.convertToSalaryDTO(payment);
    }

    @Override
    @Transactional
    public SalaryPaymentDTO updateSalaryPayment(Long paymentId, SalaryPaymentDTO salaryPaymentDTO) {
        SalaryPayment existingPayment = salaryPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary payment not found with id: " + paymentId));

        if (salaryPaymentDTO.getAmount() != null) {
            existingPayment.setAmount(salaryPaymentDTO.getAmount());
        }
        if (salaryPaymentDTO.getPaymentDate() != null) {
            existingPayment.setPaymentDate(salaryPaymentDTO.getPaymentDate());
        }
        if (salaryPaymentDTO.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(salaryPaymentDTO.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + salaryPaymentDTO.getEmployeeId()));
            existingPayment.setEmployee(employee);
        }
        if (salaryPaymentDTO.getPaymentReference() != null) {
            existingPayment.setPaymentReference(salaryPaymentDTO.getPaymentReference());
        }

        SalaryPayment updatedPayment = salaryPaymentRepository.save(existingPayment);
        return employeeConverter.convertToSalaryDTO(updatedPayment);
    }

    @Override
    @Transactional
    public void deleteSalaryPayment(Long paymentId) {
        if (!salaryPaymentRepository.existsById(paymentId)) {
            throw new ResourceNotFoundException("Salary payment not found with id: " + paymentId);
        }
        salaryPaymentRepository.deleteById(paymentId);
    }

    private String generatePaymentReference() {
        return "PAY-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryReceiptDTO generateSalaryReceipt(Long paymentId) {
        SalaryPayment payment = salaryPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary payment not found with id: " + paymentId));

        Employee employee = payment.getEmployee();

        // Get tasks completed during the payment period
        List<Task> tasks = taskRepository.findByEmployeeAndStatusAndValidationTimeBetween(
                employee,
                Task.TaskStatus.COMPLETED,
                payment.getPaymentDate().atStartOfDay().minusDays(30),
                payment.getPaymentDate().atTime(23, 59, 59)
        );

        // Calculate productivity metrics
        BigDecimal totalExpectedHours = tasks.stream()
                .map(t -> t.getExpectedHours() != null ?
                        BigDecimal.valueOf(t.getExpectedHours()) :
                        BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActualHours = tasks.stream()
                .map(t -> t.getActualHours() != null ?
                        BigDecimal.valueOf(t.getActualHours()) :
                        BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overallProductivity = totalExpectedHours.compareTo(BigDecimal.ZERO) > 0 ?
                totalActualHours.divide(totalExpectedHours, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        // Build the receipt DTO
        return SalaryReceiptDTO.builder()
                .receiptNumber("RCPT-" + payment.getPaymentReference())
                .issueDate(LocalDate.now())
                .employee(EmployeeInfoDTO.builder()
                        .id(employee.getId())
                        .name(employee.getName())
                        .department(employee.getDepartment().name())
                        .employmentDate(employee.getDateOfEmployment())
                        .build())
                .salary(SalaryInfoDTO.builder()
                        .amount(payment.getAmount())
                        .paymentDate(payment.getPaymentDate())
                        .paymentReference(payment.getPaymentReference())
                        .status(payment.getStatus().name())
                        .build())
                .tasks(tasks.stream()
                        .map(t -> {
                            BigDecimal expected = t.getExpectedHours() != null ?
                                    BigDecimal.valueOf(t.getExpectedHours()) : BigDecimal.ZERO;
                            BigDecimal actual = t.getActualHours() != null ?
                                    BigDecimal.valueOf(t.getActualHours()) : BigDecimal.ZERO;
                            BigDecimal completion = expected.compareTo(BigDecimal.ZERO) > 0 ?
                                    actual.divide(expected, 4, RoundingMode.HALF_UP)
                                            .multiply(BigDecimal.valueOf(100)) :
                                    BigDecimal.ZERO;

                            return TaskProductivityDTO.builder()
                                    .title(t.getTitle())
                                    .expectedHours(expected)
                                    .actualHours(actual)
                                    .completionRate(completion)
                                    .status(t.getStatus().name())
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .productivitySummary(ProductivitySummaryDTO.builder()
                        .totalExpectedHours(totalExpectedHours)
                        .totalActualHours(totalActualHours)
                        .overallProductivity(overallProductivity)
                        .workingDays(employee.getWorkingDaysCount())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public byte[] generatePdfReceipt(Long paymentId) {
        SalaryReceiptDTO receipt = generateSalaryReceipt(paymentId);
        return receiptGeneratorService.generatePdfReceipt(receipt);
    }

}