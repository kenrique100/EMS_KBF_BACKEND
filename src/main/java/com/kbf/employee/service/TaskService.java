package com.kbf.employee.service;

import com.kbf.employee.dto.TaskDTO;
import java.util.List;

public interface TaskService {
    TaskDTO createTask(TaskDTO taskDTO);
    List<TaskDTO> getAllTasks();
    List<TaskDTO> getAllTasksForEmployee(Long employeeId);
    TaskDTO updateTaskStatus(Long taskId, String action, Long currentUserId, boolean isAdmin);
    TaskDTO updateTask(Long taskId, TaskDTO taskDTO);
    void deleteTask(Long taskId);
    TaskDTO getTaskById(Long taskId);
}