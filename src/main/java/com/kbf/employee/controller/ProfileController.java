package com.kbf.employee.controller;

import com.kbf.employee.dto.EmployeeProfileDTO;
import com.kbf.employee.security.UserPrincipal;
import com.kbf.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile", description = "Employee Profile Management API")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final EmployeeService employeeService;

    @Operation(summary = "Get current user's profile with all details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping
    public ResponseEntity<EmployeeProfileDTO> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        EmployeeProfileDTO profile = employeeService.getEmployeeProfile(userPrincipal.getId());
        return ResponseEntity.ok(profile);
    }
}