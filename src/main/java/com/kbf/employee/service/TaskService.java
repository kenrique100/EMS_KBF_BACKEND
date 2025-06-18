// TaskService.java
package com.kbf.employee.service;

import com.kbf.employee.dto.TaskDTO;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Task;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        Employee employee = employeeRepository.findById(taskDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + taskDTO.getEmployeeId()));

        Task task = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .deadline(taskDTO.getDeadline())
                .employee(employee)
                .status(Task.TaskStatus.PENDING)
                .expectedHours(taskDTO.getExpectedHours())
                .build();

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getAllTasksForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return taskRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long taskId, String action) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        switch (action) {
            case "START":
                if (task.getStatus() != Task.TaskStatus.PENDING) {
                    throw new IllegalStateException("Task can only be started from PENDING status");
                }
                task.setStatus(Task.TaskStatus.IN_PROGRESS);
                task.setStartTime(LocalDateTime.now());
                break;
            case "STOP":
                if (task.getStatus() != Task.TaskStatus.IN_PROGRESS) {
                    throw new IllegalStateException("Task can only be stopped when IN_PROGRESS");
                }
                task.setStopTime(LocalDateTime.now());
                if (task.getStartTime() != null) {
                    Duration duration = Duration.between(task.getStartTime(), task.getStopTime());
                    task.setActualHours(duration.toHours() + (duration.toMinutesPart() / 60.0));
                }
                break;
            case "COMPLETE":
                task.setStatus(Task.TaskStatus.COMPLETED);
                task.setStopTime(LocalDateTime.now());
                if (task.getStartTime() != null) {
                    Duration duration = Duration.between(task.getStartTime(), task.getStopTime());
                    task.setActualHours(duration.toHours() + (duration.toMinutesPart() / 60.0));
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }
    @Transactional
    public TaskDTO updateTask(Long taskId, TaskDTO taskDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Update only the fields that are provided in the DTO
        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }
        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getDeadline() != null) {
            task.setDeadline(taskDTO.getDeadline());
        }
        if (taskDTO.getExpectedHours() != null) {
            task.setExpectedHours(taskDTO.getExpectedHours());
        }
        if (taskDTO.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(taskDTO.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + taskDTO.getEmployeeId()));
            task.setEmployee(employee);
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    private TaskDTO convertToDTO(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .employeeId(task.getEmployee().getId())
                .employeeName(task.getEmployee().getName())
                .status(task.getStatus())
                .expectedHours(task.getExpectedHours())
                .actualHours(task.getActualHours())
                .startTime(task.getStartTime())
                .stopTime(task.getStopTime())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }
    public TaskDTO getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return convertToDTO(task);
    }
}