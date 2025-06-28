package com.kbf.employee.service;

import com.kbf.employee.dto.request.TaskActionDTO;
import com.kbf.employee.dto.request.TaskDTO;
import com.kbf.employee.dto.request.TaskValidationDTO;
import com.kbf.employee.dto.response.ProductivityStatsDTO;

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
    void processUnvalidatedTasks();
    void updateDailyProductivity();
    void resetProductivityMetrics();
    ProductivityStatsDTO getProductivityStats(Long employeeId);
    TaskDTO validateTask(TaskValidationDTO validationDTO, Long adminId);
}
