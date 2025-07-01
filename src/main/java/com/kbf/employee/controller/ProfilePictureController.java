package com.kbf.employee.controller;

import com.kbf.employee.dto.request.ProfilePictureUploadDTO;
import com.kbf.employee.exception.AccessDeniedException;
import com.kbf.employee.security.UserPrincipal;
import com.kbf.employee.service.EmployeeProfilePictureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile Picture", description = "Employee Profile Picture Management API")
@RestController
@RequestMapping("/api/profile-pictures")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfilePictureController {

    private final EmployeeProfilePictureService profilePictureService;

    @Operation(summary = "Upload profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping(value = "/{employeeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Long employeeId,
            @ModelAttribute ProfilePictureUploadDTO uploadDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (!userPrincipal.isAdmin() && !employeeId.equals(userPrincipal.getId())) {
            throw new AccessDeniedException("You can only upload your own profile picture");
        }

        String filePath = profilePictureService.uploadProfilePicture(
                employeeId,
                uploadDTO.getProfilePicture()
        );

        return ResponseEntity.ok(filePath);
    }

    @Operation(summary = "Remove profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile picture removed successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> removeProfilePicture(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (!userPrincipal.isAdmin() && !employeeId.equals(userPrincipal.getId())) {
            throw new AccessDeniedException("You can only remove your own profile picture");
        }

        profilePictureService.removeProfilePicture(employeeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture retrieved"),
            @ApiResponse(responseCode = "404", description = "Profile picture not found")
    })
    @GetMapping("/{employeeId}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable Long employeeId) {
        Resource resource = profilePictureService.getProfilePicture(employeeId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.IMAGE_JPEG) // or detect from file extension
                .body(resource);
    }

    @Operation(summary = "Get profile picture thumbnail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture thumbnail retrieved"),
            @ApiResponse(responseCode = "404", description = "Profile picture thumbnail not found")
    })
    @GetMapping("/{employeeId}/thumbnail")
    public ResponseEntity<Resource> getProfilePictureThumbnail(@PathVariable Long employeeId) {
        Resource resource = profilePictureService.getProfilePictureThumbnail(employeeId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.IMAGE_JPEG) // or detect from file extension
                .body(resource);
    }
}