package com.kbf.employee.repository;

import com.kbf.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByNationalId(String nationalId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByNationalId(String nationalId);
    List<Employee> findByStatusExpirationBeforeAndStatusIn(
            LocalDateTime expirationTime,
            List<Employee.EmployeeStatus> statuses
    );
    List<Employee> findByStatusAndTerminationTimestampBefore(
            Employee.EmployeeStatus status,
            LocalDateTime timestamp
    );
    List<Employee> findByStatus(Employee.EmployeeStatus status);

    @Query("SELECT DISTINCT t.employee FROM Task t WHERE DATE(t.deadline) = :date")
    List<Employee> findEmployeesWithTasksDue(@Param("date") LocalDate date);
}