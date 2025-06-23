package com.kbf.employee.service.impl;

import com.kbf.employee.dto.SalaryPaymentDTO;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.SalaryPayment;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.SalaryPaymentRepository;
import com.kbf.employee.service.SalaryService;
import com.kbf.employee.util.EmployeeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final SalaryPaymentRepository salaryPaymentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeConverter employeeConverter;


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
}