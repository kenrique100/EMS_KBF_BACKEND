package com.kbf.employee.repository;

import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByEmployee(Employee employee);
    List<Task> findByDeadlineBeforeAndStatus(Date deadline, Task.TaskStatus status);
    List<Task> findByStatusAndStopTimeBetween(Task.TaskStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.employee = :employee AND t.createdAt BETWEEN :start AND :end")
    List<Task> findByEmployeeAndCreatedAtBetween(
            @Param("employee") Employee employee,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Task> findByEmployeeAndStatusAndStopTimeBetween(
            Employee employee,
            Task.TaskStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Task> findByStatusAndIsValidatedAndStopTimeBetween(
            Task.TaskStatus status,
            Boolean isValidated,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Task> findByEmployeeAndIsValidated(Employee employee, Boolean isValidated);

    @Query("SELECT t FROM Task t WHERE " +
            "t.employee = :employee AND " +
            "t.status = :status AND " +
            "t.validationTime BETWEEN :start AND :end")
    List<Task> findByEmployeeAndStatusAndValidationTimeBetween(
            @Param("employee") Employee employee,
            @Param("status") Task.TaskStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT t FROM Task t WHERE " +
            "t.validationTime BETWEEN :start AND :end")
    List<Task> findByValidationTimeBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}