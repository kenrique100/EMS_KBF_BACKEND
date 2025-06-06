package com.kbf.employee.service;

import com.kbf.employee.dto.SalaryPaymentDTO;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.SalaryPayment;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.SalaryPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryPaymentRepository salaryPaymentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public SalaryPaymentDTO createSalaryPayment(SalaryPaymentDTO salaryPaymentDTO) {
        Employee employee = employeeRepository.findById(salaryPaymentDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + salaryPaymentDTO.getEmployeeId()));

        SalaryPayment payment = new SalaryPayment();
        payment.setAmount(salaryPaymentDTO.getAmount());
        payment.setPaymentDate(salaryPaymentDTO.getPaymentDate());
        payment.setEmployee(employee);
        payment.setStatus(SalaryPayment.PaymentStatus.PROCESSED);
        payment.setPaymentReference(generatePaymentReference());

        SalaryPayment savedPayment = salaryPaymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }

    public List<SalaryPaymentDTO> getSalaryPaymentsForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return salaryPaymentRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SalaryPaymentDTO getSalaryPaymentById(Long paymentId) {
        SalaryPayment payment = salaryPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary payment not found with id: " + paymentId));
        return convertToDTO(payment);
    }

    @Transactional
    public void deleteSalaryPayment(Long paymentId) {
        SalaryPayment payment = salaryPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary payment not found with id: " + paymentId));
        salaryPaymentRepository.delete(payment);
    }

    private String generatePaymentReference() {
        return "PAY-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private SalaryPaymentDTO convertToDTO(SalaryPayment payment) {
        SalaryPaymentDTO dto = new SalaryPaymentDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setEmployeeId(payment.getEmployee().getId());
        dto.setStatus(payment.getStatus());
        dto.setPaymentReference(payment.getPaymentReference());
        return dto;
    }
}