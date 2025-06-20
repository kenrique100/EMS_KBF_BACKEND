package com.kbf.employee.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("employeeSecurity")
public class EmployeeSecurity {

    public boolean isOwner(Authentication authentication, Long employeeId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId().equals(employeeId);
    }
}