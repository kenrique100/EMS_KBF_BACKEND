package com.kbf.employee.service;

import com.kbf.employee.dto.SalaryPaymentDTO;
import java.util.List;

public interface SalaryService {
    SalaryPaymentDTO createSalaryPayment(SalaryPaymentDTO salaryPaymentDTO);
    List<SalaryPaymentDTO> getAllSalaryPayments();
    List<SalaryPaymentDTO> getSalaryPaymentsForEmployee(Long employeeId);
    SalaryPaymentDTO getSalaryPaymentById(Long paymentId);
    SalaryPaymentDTO updateSalaryPayment(Long paymentId, SalaryPaymentDTO salaryPaymentDTO);
    void deleteSalaryPayment(Long paymentId);
}