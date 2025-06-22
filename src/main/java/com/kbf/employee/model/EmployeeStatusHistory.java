package com.kbf.employee.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_status_history")
public class EmployeeStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private Employee.EmployeeStatus status;

    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private Duration allocatedDuration;
    private Duration actualDuration;
    private LocalDateTime expectedEndTimestamp;
}