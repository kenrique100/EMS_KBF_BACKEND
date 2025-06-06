package com.kbf.employee.security;

import com.kbf.employee.model.Employee;
import com.kbf.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> {
                    String errorMsg = "User not found with username: " + username;
                    log.error(errorMsg);
                    return new UsernameNotFoundException(errorMsg);
                });

        log.debug("User found: {}", employee.getUsername());
        return UserPrincipal.create(employee);
    }
}