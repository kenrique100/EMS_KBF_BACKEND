package com.kbf.employee.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kbf.employee.model.enums.Department;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Department department;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "date_of_employment", nullable = false)
    private LocalDate dateOfEmployment;

    @Column(name = "profile_picture_path")
    private String profilePicturePath;

    @Column(name = "document_path")
    private String documentPath;

    @Column(name = "total_hours_worked_last_30_days", columnDefinition = "double default 0.0")
    private Double totalHoursWorkedLast30Days = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    @Column(name = "status_change_timestamp")
    private LocalDateTime statusChangeTimestamp;

    @Column(name = "status_expiration")
    private LocalDateTime statusExpiration;

    @Column(name = "suspension_duration")
    private Duration suspensionDuration;

    @Column(name = "termination_timestamp")
    private LocalDateTime terminationTimestamp;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer workingDaysCount = 0;

    @Column(name = "last_productivity_reset_date")
    private LocalDate lastProductivityResetDate;

    @Column(name = "current_period_start_date", columnDefinition = "date default CURRENT_DATE")
    private LocalDate currentPeriodStartDate = LocalDate.now();

    @Column(name = "total_productive_days", columnDefinition = "integer default 0")
    private Integer totalProductiveDays = 0;

    @Column(name = "last_productivity_update")
    private LocalDate lastProductivityUpdate;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeStatusHistory> statusHistory = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "employee_roles",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void initializeFields() {
        if (this.workingDaysCount == null) {
            this.workingDaysCount = 0;
        }
        if (this.totalHoursWorkedLast30Days == null) {
            this.totalHoursWorkedLast30Days = 0.0;
        }
        if (this.currentPeriodStartDate == null) {
            this.currentPeriodStartDate = LocalDate.now();
        }
        if (this.totalProductiveDays == null) {
            this.totalProductiveDays = 0;
        }
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE, SUSPENDED, TERMINATED
    }
}