package com.kbf.employee.repository;

import com.kbf.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Employee> findByStatusExpirationBeforeAndStatusIn(
            LocalDateTime expirationTime,
            List<Employee.EmployeeStatus> statuses
    );
    List<Employee> findByStatusAndTerminationTimestampBefore(
            Employee.EmployeeStatus status,
            LocalDateTime timestamp
    );

    Optional<Employee> findByProfilePicturePathOrDocumentPath(
            String profilePicturePath,
            String documentPath
    );
}