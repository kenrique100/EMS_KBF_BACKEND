package com.kbf.employee.controller;

import com.kbf.employee.dto.TaskActionDTO;
import com.kbf.employee.dto.TaskDTO;
import com.kbf.employee.model.Task;
import com.kbf.employee.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Task", description = "Task Management API")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create a new task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.ok(taskService.createTask(taskDTO));
    }

    @Operation(summary = "Get all tasks")
    @ApiResponse(responseCode = "200", description = "List of all tasks")
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @Operation(summary = "Get all tasks for an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TaskDTO>> getTasksForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(taskService.getAllTasksForEmployee(employeeId));
    }

    @Operation(summary = "Get task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @Operation(summary = "Update task status (start, stop, complete)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid action or state"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(@Valid @RequestBody TaskActionDTO actionDTO) {
        return ResponseEntity.ok(taskService.updateTaskStatus(actionDTO));
    }

    @Operation(summary = "Delete a task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get tasks by status for an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/employee/{employeeId}/status/{status}")
    public ResponseEntity<List<TaskDTO>> getTasksByStatusForEmployee(
            @PathVariable Long employeeId,
            @PathVariable Task.TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatusForEmployee(employeeId, status));
    }
}