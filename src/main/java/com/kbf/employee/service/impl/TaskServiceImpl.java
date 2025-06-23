// TaskServiceImpl.java
package com.kbf.employee.service.impl;

import com.kbf.employee.dto.TaskDTO;
import com.kbf.employee.exception.AccessDeniedException;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Task;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.TaskRepository;
import com.kbf.employee.service.TaskService;
import com.kbf.employee.util.EmployeeConverter;
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
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeConverter employeeConverter;

    @Override
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
        return employeeConverter.convertToTaskDTO(savedTask);
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(employeeConverter::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getAllTasksForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return taskRepository.findByEmployee(employee).stream()
                .map(employeeConverter::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskDTO updateTaskStatus(Long taskId, String action, Long currentUserId, boolean isAdmin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!isAdmin && !task.getEmployee().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to modify this task");
        }

        switch (action.toUpperCase()) {
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

        return employeeConverter.convertToTaskDTO(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Long taskId, TaskDTO taskDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

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
        return employeeConverter.convertToTaskDTO(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    @Override
    public TaskDTO getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return employeeConverter.convertToTaskDTO(task);
    }
}