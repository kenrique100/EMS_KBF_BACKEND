package com.kbf.employee.repository;

import com.kbf.employee.model.Employee;
import com.kbf.employee.model.SalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, Long> {
    List<SalaryPayment> findByEmployee(Employee employee);
    void deleteByEmployeeId(Long employeeId);
}