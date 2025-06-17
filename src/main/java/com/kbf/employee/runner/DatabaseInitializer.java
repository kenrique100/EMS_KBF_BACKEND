package com.kbf.employee.runner;

import com.kbf.employee.model.Department;
import com.kbf.employee.model.Employee;
import com.kbf.employee.model.Role;
import com.kbf.employee.model.Role.RoleName;
import com.kbf.employee.repository.EmployeeRepository;
import com.kbf.employee.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Ensure ROLE_ADMIN exists and retrieve it
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));

        // Ensure ROLE_USER exists
        roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));

        // Create admin user if it doesn't exist
        if (!employeeRepository.existsByUsername("admin")) {
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);

            Employee admin = Employee.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .name("System Administrator")
                    .email("ngwakenri2016@gmail.com")
                    .phoneNumber("+237670466987")
                    .dateOfEmployment(LocalDate.now())
                    .department(Department.ADMINISTRATION)
                    .status(Employee.EmployeeStatus.ACTIVE)
                    .roles(adminRoles)
                    .build();

            employeeRepository.save(admin);
        }
    }
}