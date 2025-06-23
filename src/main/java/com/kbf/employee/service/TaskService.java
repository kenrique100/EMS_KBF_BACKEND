package com.kbf.employee.service;

import com.kbf.employee.dto.ProductivityStatsDTO;
import com.kbf.employee.dto.TaskDTO;
import com.kbf.employee.dto.TaskActionDTO;

import java.util.List;

public interface TaskService {
    TaskDTO createTask(TaskDTO taskDTO);
    List<TaskDTO> getAllTasks();
    List<TaskDTO> getAllTasksForEmployee(Long employeeId);
    TaskDTO updateTaskStatus(TaskActionDTO actionDTO, Long currentUserId, boolean isAdmin);
    TaskDTO updateTask(Long taskId, TaskDTO taskDTO);
    void deleteTask(Long taskId);
    TaskDTO getTaskById(Long taskId);
    void processExpiredTasks();
    void updateDailyProductivity();
    void resetProductivityMetrics();
    ProductivityStatsDTO getProductivityStats(Long employeeId);
}