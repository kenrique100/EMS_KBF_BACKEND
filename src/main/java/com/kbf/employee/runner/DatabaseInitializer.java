/*
package com.kbf.employee.runner;

import com.kbf.employee.model.enums.Department;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.model.Role.RoleName;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            Role adminRole = createRoleIfNotExists(RoleName.ROLE_ADMIN);
            createRoleIfNotExists(RoleName.ROLE_USER);

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
}*/
package com.kbf.employee.runner;

import com.kbf.employee.model.enums.Department;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.model.Role.RoleName;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            Role adminRole = createRoleIfNotExists(RoleName.ROLE_ADMIN);
            createRoleIfNotExists(RoleName.ROLE_USER);

            if (!employeeRepository.existsByUsername("admin")) {
                createAdminUser(adminRole);
            }
        } catch (InvalidDataAccessResourceUsageException e) {
            log.warn("Skipping DatabaseInitializer - database schema not ready yet: {}", e.getMessage());
            // DO NOT rethrow here
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