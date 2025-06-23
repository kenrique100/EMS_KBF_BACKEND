package com.kbf.employee.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date deadline;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "expected_hours")
    private Integer expectedHours;

    @Column(name = "actual_hours")
    private Double actualHours;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "stop_time")
    private LocalDateTime stopTime;

    @Column(name = "last_resume_time")
    private LocalDateTime lastResumeTime;

    @Column(name = "total_worked_minutes")
    private Long totalWorkedMinutes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        INCOMPLETED,
        CANCELLED
    }

    @PreUpdate
    public void calculateActualHours() {
        if (startTime != null && stopTime != null) {
            long minutesWorked = Duration.between(startTime, stopTime).toMinutes();
            if (lastResumeTime != null && stopTime.isAfter(lastResumeTime)) {
                minutesWorked += Duration.between(lastResumeTime, stopTime).toMinutes();
            }
            this.totalWorkedMinutes = minutesWorked;
            this.actualHours = minutesWorked / 60.0;
        }
    }
}