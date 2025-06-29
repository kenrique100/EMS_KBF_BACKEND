package com.kbf.employee.service.impl;

import com.kbf.employee.dto.request.TaskActionDTO;
import com.kbf.employee.dto.request.TaskDTO;
import com.kbf.employee.dto.request.TaskValidationDTO;
import com.kbf.employee.dto.response.ProductivityStatsDTO;
import com.kbf.employee.exception.*;
import com.kbf.employee.model.*;
import com.kbf.employee.repository.*;
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
                .isValidated(false)
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
    @Transactional
    public TaskDTO validateTask(TaskValidationDTO validationDTO, Long adminId) {
        Task task = taskRepository.findById(validationDTO.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getStatus() != Task.TaskStatus.COMPLETED) {
            throw new InvalidOperationException("Only completed tasks can be validated");
        }

        Employee admin = employeeRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRoles().stream().noneMatch(role -> role.getName() == Role.RoleName.ROLE_ADMIN)) {
            throw new AccessDeniedException("Only admins can validate tasks");
        }

        task.setIsValidated(validationDTO.getApprove());
        task.setValidationTime(LocalDateTime.now());

        if (validationDTO.getApprove()) {
            updateEmployeeProductivity(task.getEmployee(), task.getValidationTime().toLocalDate());
        } else {
            task.setStatus(Task.TaskStatus.UNCOMPLETED);
        }

        return employeeConverter.convertToTaskDTO(taskRepository.save(task));
    }

    @Override
    @Scheduled(cron = "0 */5 * * * *")
    public void processExpiredTasks() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayDate = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Task> expiredTasks = taskRepository.findByDeadlineBeforeAndStatus(yesterdayDate, Task.TaskStatus.PENDING);

        expiredTasks.forEach(task -> {
            task.setStatus(Task.TaskStatus.UNCOMPLETED);
            taskRepository.save(task);
        });
    }

    @Override
    @Scheduled(cron = "0 */5 * * * *")
    public void processUnvalidatedTasks() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

        List<Task> unvalidatedTasks = taskRepository.findByStatusAndIsValidatedAndStopTimeBetween(
                Task.TaskStatus.COMPLETED,
                false,
                startOfDay,
                endOfDay
        );

        unvalidatedTasks.forEach(task -> {
            task.setStatus(Task.TaskStatus.UNCOMPLETED);
            taskRepository.save(task);
            log.info("Marked task {} as UNCOMPLETED (not validated by deadline)", task.getId());
        });
    }

    @Override
    public ProductivityStatsDTO getProductivityStats(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<Task> validatedTasks = taskRepository.findByEmployeeAndIsValidated(employee, true);
        double totalHoursWorked = calculateTotalHours(validatedTasks);
        int workingDays = getDistinctWorkingDays(validatedTasks);
        double dailyAverage = workingDays > 0 ? totalHoursWorked / workingDays : 0.0;

        return ProductivityStatsDTO.builder()
                .totalHoursWorked(totalHoursWorked)
                .dailyAverage(dailyAverage)
                .workingDays(workingDays)
                .periodStartDate(employee.getCurrentPeriodStartDate())
                .periodEndDate(employee.getCurrentPeriodStartDate().plusDays(29))
                .productivityPercentage(calculateProductivityPercentage(employee))
                .build();
    }

    private int getDistinctWorkingDays(List<Task> tasks) {
        return (int) tasks.stream()
                .map(t -> t.getStopTime().toLocalDate())
                .distinct()
                .count();
    }

    private double calculateTotalHours(List<Task> tasks) {
        return tasks.stream()
                .mapToDouble(t -> t.getActualHours() != null ? t.getActualHours() : 0)
                .sum();
    }

    private Double calculateProductivityPercentage(Employee employee) {
        List<Task> validatedTasks = taskRepository.findByEmployeeAndIsValidated(employee, true);

        double totalExpectedHours = validatedTasks.stream()
                .mapToDouble(t -> t.getExpectedHours() != null ? t.getExpectedHours() : 0)
                .sum();

        double totalActualHours = employee.getTotalHoursWorkedLast30Days() != null ?
                employee.getTotalHoursWorkedLast30Days() : 0;

        return totalExpectedHours == 0 ? 0.0 : (totalActualHours / totalExpectedHours) * 100;
    }

    private void updateWorkedTime(Task task) {
        if (task.getStartTime() == null || task.getStopTime() == null) {
            return;
        }

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
        if (task.getStartTime() == null || task.getStopTime() == null) {
            return 0;
        }

        Duration duration = Duration.between(task.getStartTime(), task.getStopTime());
        long seconds = duration.getSeconds();
        return (seconds / 60) + (seconds % 60 >= 30 ? 1 : 0);
    }

    private List<Task> getCompletedTasksForDay(Employee employee, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return taskRepository.findByEmployeeAndStatusAndValidationTimeBetween(
                employee,
                Task.TaskStatus.COMPLETED,
                startOfDay,
                endOfDay
        );
    }

    private void updateEmployeeProductivity(Employee employee, LocalDate date) {
        if (!date.equals(employee.getLastProductivityUpdate())) {
            List<Task> completedTasks = getCompletedTasksForDay(employee, date);
            double dailyHours = calculateTotalHours(completedTasks);

            if (dailyHours > 0) {
                employee.setWorkingDaysCount(employee.getWorkingDaysCount() + 1);
                employee.setTotalHoursWorkedLast30Days(
                        employee.getTotalHoursWorkedLast30Days() + dailyHours
                );
                employee.setLastProductivityUpdate(date);
                employeeRepository.save(employee);
            }
        }
    }

    @Override
    @Scheduled(cron = "0 */5 * * * *")
    public void updateDailyProductivity() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Employee> activeEmployees = employeeRepository.findByStatus(Employee.EmployeeStatus.ACTIVE);

        activeEmployees.forEach(employee -> {
            if (!yesterday.equals(employee.getLastProductivityUpdate())) {
                List<Task> completedTasks = getCompletedTasksForDay(employee, yesterday);
                double dailyHours = calculateTotalHours(completedTasks);

                if (dailyHours > 0) {
                    employee.setWorkingDaysCount(employee.getWorkingDaysCount() + 1);
                    employee.setTotalHoursWorkedLast30Days(
                            employee.getTotalHoursWorkedLast30Days() + dailyHours
                    );
                    employee.setLastProductivityUpdate(yesterday);
                    employeeRepository.save(employee);
                }
            }
        });
    }

    @Override
    @Scheduled(cron = "0 */5 * * * *")
    public void resetProductivityMetrics() {
        List<Employee> allEmployees = employeeRepository.findAll();

        log.info("Resetting productivity metrics for {} employees", allEmployees.size());
        allEmployees.forEach(employee -> {
            employee.setTotalHoursWorkedLast30Days(0.0);
            employee.setWorkingDaysCount(0);
            employee.setLastProductivityResetDate(LocalDate.now());
            employee.setCurrentPeriodStartDate(LocalDate.now());
            employeeRepository.save(employee);
        });
        log.info("Completed resetting productivity metrics");
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