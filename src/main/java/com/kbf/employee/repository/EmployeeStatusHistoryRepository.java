package com.kbf.employee.repository;

import com.kbf.employee.model.EmployeeStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeStatusHistoryRepository extends JpaRepository<EmployeeStatusHistory, Long> {
    List<EmployeeStatusHistory> findByEmployeeIdOrderByStartTimestampDesc(Long employeeId);
    Optional<EmployeeStatusHistory> findByEmployeeIdAndEndTimestampIsNull(Long employeeId);
    void deleteByEmployeeId(Long employeeId);

}