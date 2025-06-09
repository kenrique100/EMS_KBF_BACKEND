package com.kbf.employee.service;

import com.kbf.employee.dto.TaskDTO;
import com.kbf.employee.dto.TaskActionDTO;
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
                .build();

        if (taskDTO.getExpectedHours() != null) {
            task.setExpectedHours(Duration.ofHours(taskDTO.getExpectedHours()));
        }

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

    public List<TaskDTO> getTasksByStatusForEmployee(Long employeeId, Task.TaskStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return taskRepository.findByEmployeeAndStatus(employee, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return convertToDTO(task);
    }

    @Transactional
    public TaskDTO updateTaskStatus(TaskActionDTO actionDTO) {
        Task task = taskRepository.findById(actionDTO.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + actionDTO.getTaskId()));

        switch (actionDTO.getAction().toUpperCase()) {
            case "START":
                if (task.getStatus() != Task.TaskStatus.PENDING) {
                    throw new IllegalStateException("Task can only be started from PENDING status");
                }
                task.setStartTime(LocalDateTime.now());
                task.setStatus(Task.TaskStatus.IN_PROGRESS);
                break;
            case "STOP":
                if (task.getStatus() != Task.TaskStatus.IN_PROGRESS) {
                    throw new IllegalStateException("Task can only be stopped when IN_PROGRESS");
                }
                task.setStopTime(LocalDateTime.now());
                break;
            case "COMPLETE":
                if (task.getStatus() != Task.TaskStatus.IN_PROGRESS) {
                    throw new IllegalStateException("Task can only be completed when IN_PROGRESS");
                }
                if (task.getStopTime() == null) {
                    task.setStopTime(LocalDateTime.now());
                }
                if (task.getExpectedHours() != null && task.getActualHours() != null) {
                    if (task.getActualHours().compareTo(task.getExpectedHours()) < 0) {
                        task.setStatus(Task.TaskStatus.UNCOMPLETED);
                    } else {
                        task.setStatus(Task.TaskStatus.COMPLETED);
                    }
                } else {
                    task.setStatus(Task.TaskStatus.COMPLETED);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + actionDTO.getAction());
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDeadline(task.getDeadline());
        dto.setEmployeeId(task.getEmployee().getId());
        dto.setStatus(task.getStatus());
        dto.setStartTime(task.getStartTime());
        dto.setStopTime(task.getStopTime());

        if (task.getExpectedHours() != null) {
            dto.setExpectedHours(task.getExpectedHours().toHours());
        }

        if (task.getActualHours() != null) {
            dto.setActualHours(task.getActualHours().toMinutes() / 60.0);
        }

        return dto;
    }
}