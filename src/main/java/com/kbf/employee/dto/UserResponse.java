package com.kbf.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String name;
    private String email;
    private List<String> roles;
}