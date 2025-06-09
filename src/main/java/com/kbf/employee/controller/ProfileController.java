package com.kbf.employee.controller;

import com.kbf.employee.dto.EmployeeProfileDTO;
import com.kbf.employee.dto.SalaryPaymentDTO;
import com.kbf.employee.dto.TaskDTO;
import com.kbf.employee.security.UserPrincipal;
import com.kbf.employee.service.EmployeeService;
import com.kbf.employee.service.SalaryService;
import com.kbf.employee.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final EmployeeService employeeService;
    private final SalaryService salaryService;
    private final TaskService taskService;

    @Operation(summary = "Get current user's profile")
    @GetMapping
    public ResponseEntity<EmployeeProfileDTO> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(employeeService.getEmployeeProfile(userPrincipal.getId()));
    }

    @Operation(summary = "Get current user's salary details")
    @GetMapping("/my-salary")
    public ResponseEntity<List<SalaryPaymentDTO>> getMySalary(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(salaryService.getSalaryPaymentsForEmployee(userPrincipal.getId()));
    }

    @Operation(summary = "Get current user's tasks")
    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskDTO>> getMyTasks(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(taskService.getAllTasksForEmployee(userPrincipal.getId()));
    }
}