package com.kbf.employee.runner;

import com.kbf.employee.model.enums.Department;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.model.Role.RoleName;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {
        try {
            // Create roles if they don't exist
            Role adminRole = createRoleIfNotExists(RoleName.ROLE_ADMIN);
            createRoleIfNotExists(RoleName.ROLE_USER);

            // Create admin user if it doesn't exist
            if (!employeeRepository.existsByUsername("admin")) {
                createAdminUser(adminRole);
            }
        } catch (DataAccessException e) {
            log.error("Failed to initialize database: {}", e.getMessage());
            throw e;
        }
    }

    private Role createRoleIfNotExists(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    log.info("Creating role: {}", roleName);
                    return roleRepository.save(new Role(roleName));
                });
    }

    private void createAdminUser(Role adminRole) {
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);

        Employee admin = Employee.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin@123"))
                .name("System Administrator")
                .email("admin@example.com")
                .phoneNumber("+237670466987")
                .dateOfEmployment(LocalDate.now())
                .department(Department.ADMINISTRATION)
                .status(Employee.EmployeeStatus.ACTIVE)
                .roles(adminRoles)
                .build();

        employeeRepository.save(admin);
        log.info("Created admin user");
    }
}