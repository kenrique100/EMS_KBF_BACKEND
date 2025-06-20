package com.kbf.employee.repository;

import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByEmployee(Employee employee);

}