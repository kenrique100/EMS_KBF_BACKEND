package com.kbf.employee.runner;

import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            log.info("Initializing roles in database");
            Role adminRole = new Role(Role.RoleName.ROLE_ADMIN);
            Role userRole = new Role(Role.RoleName.ROLE_USER);
            roleRepository.save(adminRole);
            roleRepository.save(userRole);
            log.info("Roles initialized successfully");
        }
    }

    private void initializeAdminUser() {
        String adminUsername = "admin";
        if (!employeeRepository.existsByUsername(adminUsername)) {
            log.info("Creating initial admin user");

            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            Employee admin = Employee.builder()
                    .username(adminUsername)
                    .name("Administrator")
                    .password(passwordEncoder.encode("admin123"))
                    .dateOfEmployment(LocalDate.now())
                    .status(Employee.EmployeeStatus.ACTIVE)
                    .roles(Set.of(adminRole))
                    .build();

            employeeRepository.save(admin);
            log.info("Admin user created successfully with username: {}", adminUsername);
        }
    }
}