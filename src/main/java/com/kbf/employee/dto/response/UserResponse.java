package com.kbf.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private List<String> roles;
}