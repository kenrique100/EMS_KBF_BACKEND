package com.kbf.employee.service.impl;

import com.kbf.employee.dto.*;
import com.kbf.employee.exception.AccessDeniedException;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Task;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.TaskRepository;
import com.kbf.employee.service.TaskService;
import com.kbf.employee.util.EmployeeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
                .totalWorkedMinutes(0L)
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
    public TaskDTO updateTaskStatus(TaskActionDTO actionDTO, Long currentUserId, boolean isAdmin) {
        Task task = taskRepository.findById(actionDTO.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + actionDTO.getTaskId()));

        if (!isAdmin && !task.getEmployee().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to modify this task");
        }

        switch (actionDTO.getAction().toUpperCase()) {
            case "START":
                startTask(task);
                break;
            case "STOP":
                stopTask(task);
                break;
            case "CONTINUE":
                continueTask(task);
                break;
            case "COMPLETE":
                completeTask(task);
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + actionDTO.getAction());
        }

        return employeeConverter.convertToTaskDTO(taskRepository.save(task));
    }

    private void startTask(Task task) {
        if (task.getStatus() != Task.TaskStatus.PENDING) {
            throw new IllegalStateException("Task can only be started from PENDING status");
        }
        task.setStatus(Task.TaskStatus.IN_PROGRESS);
        task.setStartTime(LocalDateTime.now());
        task.setLastResumeTime(LocalDateTime.now());
    }

    private void stopTask(Task task) {
        if (task.getStatus() != Task.TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task can only be stopped when IN_PROGRESS");
        }
        task.setStopTime(LocalDateTime.now());
        updateWorkedTime(task);
    }

    private void continueTask(Task task) {
        if (task.getStatus() != Task.TaskStatus.IN_PROGRESS || task.getStopTime() == null) {
            throw new IllegalStateException("Task can only be continued when IN_PROGRESS and previously stopped");
        }
        task.setLastResumeTime(LocalDateTime.now());
        task.setStopTime(null);
    }

    private void completeTask(Task task) {
        if (task.getStatus() != Task.TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task can only be completed when IN_PROGRESS");
        }
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setStopTime(LocalDateTime.now());
        updateWorkedTime(task);
    }


    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Runs at midnight every day
    public void processExpiredTasks() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayDate = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Task> expiredTasks = taskRepository.findByDeadlineBeforeAndStatus(yesterdayDate, Task.TaskStatus.PENDING);

        expiredTasks.forEach(task -> {
            task.setStatus(Task.TaskStatus.INCOMPLETED);
            taskRepository.save(task);
        });
    }

    @Override
    public ProductivityStatsDTO getProductivityStats(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        double dailyAverage = employee.getWorkingDaysCount() > 0
                ? employee.getTotalHoursWorkedLast30Days() / employee.getWorkingDaysCount()
                : 0.0;

        return ProductivityStatsDTO.builder()
                .totalHoursWorked(employee.getTotalHoursWorkedLast30Days())
                .dailyAverage(dailyAverage)
                .workingDays(employee.getWorkingDaysCount())
                .periodStartDate(employee.getCurrentPeriodStartDate())
                .periodEndDate(employee.getCurrentPeriodStartDate().plusDays(29))
                .productivityPercentage(calculateProductivityPercentage(employee))
                .build();
    }

    private Double calculateProductivityPercentage(Employee employee) {
        // Get all tasks assigned in current period
        LocalDateTime periodStart = employee.getCurrentPeriodStartDate().atStartOfDay();
        LocalDateTime periodEnd = periodStart.plusDays(30);

        List<Task> periodTasks = taskRepository.findByEmployeeAndCreatedAtBetween(
                employee,
                periodStart,
                periodEnd
        );

        double totalExpectedHours = periodTasks.stream()
                .mapToDouble(t -> t.getExpectedHours() != null ? t.getExpectedHours() : 0)
                .sum();

        if (totalExpectedHours == 0) return 0.0;

        return (employee.getTotalHoursWorkedLast30Days() / totalExpectedHours) * 100;
    }

    private void updateWorkedTime(Task task) {
        // Only recalculate if task was actually worked on
        if (task.getStartTime() == null || task.getStopTime() == null) {
            return;
        }

        // Check if we already have up-to-date calculation
        if (isCalculationUpToDate(task)) {
            return;
        }

        long minutesWorked = calculateTotalMinutesWorked(task);
        task.setTotalWorkedMinutes(minutesWorked);
        task.setActualHours(minutesWorked / 60.0);
    }

    private boolean isCalculationUpToDate(Task task) {
        return task.getLastResumeTime() != null &&
                task.getUpdatedAt() != null &&
                task.getUpdatedAt().isBefore(task.getLastResumeTime());
    }

    private long calculateTotalMinutesWorked(Task task) {
        long minutesWorked = Duration.between(task.getStartTime(), task.getStopTime()).toMinutes();
        if (task.getLastResumeTime() != null && task.getStopTime().isAfter(task.getLastResumeTime())) {
            minutesWorked += Duration.between(task.getLastResumeTime(), task.getStopTime()).toMinutes();
        }
        return minutesWorked;
    }

    private List<Task> getCompletedTasksForDay(Employee employee, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return taskRepository.findByEmployeeAndStatusAndStopTimeBetween(
                employee,
                Task.TaskStatus.COMPLETED,
                startOfDay,
                endOfDay
        );
    }

    private double calculateDailyHours(List<Task> completedTasks) {
        return completedTasks.stream()
                .mapToDouble(t -> t.getActualHours() != null ? t.getActualHours() : 0)
                .sum();
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateDailyProductivity() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

        List<Employee> employeesWithTasks = employeeRepository.findEmployeesWithTasksDue(yesterday);
        log.info("Updating daily productivity for {} employees with tasks due on {}",
                employeesWithTasks.size(), yesterday);

        employeesWithTasks.forEach(employee -> updateEmployeeProductivity(employee, startOfDay, endOfDay));

    }

    private void updateEmployeeProductivity(Employee employee, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        employee.setWorkingDaysCount(employee.getWorkingDaysCount() + 1);
        employee.setTotalProductiveDays(employee.getTotalProductiveDays() + 1);

        List<Task> completedTasks = getCompletedTasksForDay(employee, startOfDay, endOfDay);
        double dailyHours = calculateDailyHours(completedTasks);

        employee.setTotalHoursWorkedLast30Days(
                employee.getTotalHoursWorkedLast30Days() + dailyHours
        );

        employeeRepository.save(employee);
        log.debug("Updated productivity for employee {}: +{} hours (total: {})",
                employee.getId(), dailyHours, employee.getTotalHoursWorkedLast30Days());
    }

    @Override
    @Scheduled(cron = "0 0 0 1 * ?")
    public void resetProductivityMetrics() {
        List<Employee> allEmployees = employeeRepository.findAll();

        log.info("Resetting productivity metrics for {} employees", allEmployees.size());
        allEmployees.forEach(this::resetEmployeeProductivity);
        log.info("Completed resetting productivity metrics");
    }

    private void resetEmployeeProductivity(Employee employee) {
        employee.setTotalHoursWorkedLast30Days(0.0);
        employee.setWorkingDaysCount(0);
        employee.setLastProductivityResetDate(LocalDate.now());
        employee.setCurrentPeriodStartDate(LocalDate.now());
        employeeRepository.save(employee);
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